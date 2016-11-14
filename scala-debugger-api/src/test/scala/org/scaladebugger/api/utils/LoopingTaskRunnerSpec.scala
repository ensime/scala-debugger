package org.scaladebugger.api.utils
import acyclic.file

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class LoopingTaskRunnerSpec extends test.ParallelMockFunSpec with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(300, Milliseconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  private def waitDuringTest(): Unit = Thread.sleep(150)

  private val loopingTaskRunner = new LoopingTaskRunner(initialWorkers = 1)

  describe("LoopingTaskRunner") {
    describe("#start") {
      it("should throw an exception if already started") {
        loopingTaskRunner.start()

        intercept[AssertionError] {
          loopingTaskRunner.start()
        }
      }

      it("should start a pool of threads based on max workers that runs tasks") {
        val totalWorkers = 4
        val loopingTaskRunner = new LoopingTaskRunner(totalWorkers)
        val tasksExecuted = new AtomicInteger(0)

        // Queue up X * 2 tasks that are blocking
        (1 to totalWorkers * 2).foreach(_ => loopingTaskRunner.addTask({
          tasksExecuted.incrementAndGet()
          while (true) { Thread.sleep(1) }
        }))

        // Start the execution process
        loopingTaskRunner.start()

        // Verify that X tasks were executed (reflects number of workers)
        eventually {
          tasksExecuted.get() should be (totalWorkers)
        }
      }

      it("should repeatedly execute tasks") {
        val tasksExecuted = new AtomicInteger(0)

        // Queue up a single task to be executed
        loopingTaskRunner.addTask(tasksExecuted.incrementAndGet())

        // Start the execution process
        loopingTaskRunner.start()

        // Verify that the task is repeated
        eventually {
          tasksExecuted.get() should be > 1
        }
      }

      it("should only execute a task again once it has completed") {
        val loopingTaskRunner = new LoopingTaskRunner(2)
        val blockingTaskCounter = new AtomicInteger(0)
        val nonBlockingTaskCounter = new AtomicInteger(0)

        // Queue up one blocking task and one non-blocking one
        loopingTaskRunner.addTask({
          while (true) { Thread.sleep(1) }
          blockingTaskCounter.incrementAndGet()
        })
        loopingTaskRunner.addTask(nonBlockingTaskCounter.incrementAndGet())

        // Start the execution process
        loopingTaskRunner.start()

        eventually {
          blockingTaskCounter.get() should be (0)
          nonBlockingTaskCounter.get() should be > 1
        }
      }
    }

    describe("#setDesiredTotalWorkers") {
      it("should be able to increase the amount of active workers") {
        val expected = 4

        // Set a long wait period so we can ensure that we queue up workers
        val loopingTaskRunner = new LoopingTaskRunner(
          initialWorkers = 1,
          maxTaskWaitTime = (100, TimeUnit.SECONDS)
        )

        loopingTaskRunner.start()

        loopingTaskRunner.setDesiredTotalWorkers(expected)

        eventually {
          val actual = loopingTaskRunner.getCurrentActiveWorkers
          actual should be (expected)
        }
      }

      it("should be able to decrease the amount of active workers") {
        val expected = 1

        // Set a short queue time so workers can be removed
        val loopingTaskRunner = new LoopingTaskRunner(
          initialWorkers = 4,
          maxTaskWaitTime = (10, TimeUnit.MILLISECONDS)
        )

        loopingTaskRunner.start()

        loopingTaskRunner.setDesiredTotalWorkers(expected)

        eventually {
          val actual = loopingTaskRunner.getCurrentActiveWorkers
          actual should be (expected)
        }
      }
    }

    describe("#isRunning") {
      it("should return true if started") {
        loopingTaskRunner.start()

        loopingTaskRunner.isRunning should be (true)
      }

      it("should return false if not started (or started and then stopped)") {
        loopingTaskRunner.isRunning should be (false)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        intercept[AssertionError] {
          loopingTaskRunner.stop()
        }
      }

      it("should stop future execution of tasks") {
        val totalTasks = 1000
        val taskExecutionCounter = new AtomicInteger(0)

        // Add X tasks to be executed
        (1 to totalTasks).foreach(_ =>
          loopingTaskRunner.addTask(taskExecutionCounter.incrementAndGet()))

        // Start the processing of the tasks
        loopingTaskRunner.start()

        // Stop the processing of the tasks (and avoid clearing tasks to
        // prevent potential fake results)
        loopingTaskRunner.stop(removeAllTasks = false)
        waitDuringTest() // TODO: Figure out better form of allowing shutdown

        // Get the total number of executions that occurred
        val totalTaskExecutions = taskExecutionCounter.get()

        // TODO: Do we place a sleep or some other delay (or timed verification)
        //       between the first and second counter retrieval?
        waitDuringTest()

        // Verify that the number of executions has not changed, indicating
        // that no task was picked up after the stop
        taskExecutionCounter.get() should be (totalTaskExecutions)
      }

      it("should remove all tasks if flag is marked true") {
        val taskExecutionCounter = new AtomicInteger(0)

        // Add single task to increment a counter
        loopingTaskRunner.addTask(taskExecutionCounter.incrementAndGet())

        // Start the processing of the task
        loopingTaskRunner.start()

        // Stop the processing of the task
        loopingTaskRunner.stop(removeAllTasks = true)
        waitDuringTest() // TODO: Figure out better form of allowing shutdown

        // Get the total number of executions that occurred
        val totalTaskExecutions = taskExecutionCounter.get()

        // Start back up again
        loopingTaskRunner.start()

        // Verify that the number of executions has not changed, indicating
        // that the task was removed during the stop
        taskExecutionCounter.get() should be (totalTaskExecutions)
      }

      it("should not remove all tasks if flag is marked false") {
        val taskExecutionCounter = new AtomicInteger(0)

        // Add single task to increment a counter
        loopingTaskRunner.addTask(taskExecutionCounter.incrementAndGet())

        // Start the processing of the task
        loopingTaskRunner.start()

        // Stop the processing of the task
        loopingTaskRunner.stop(removeAllTasks = false)
        waitDuringTest() // TODO: Figure out better form of allowing shutdown

        // Get the total number of executions that occurred
        val totalTaskExecutions = taskExecutionCounter.get()

        // Start back up again
        loopingTaskRunner.start()

        // Verify that the number of executions has changed, indicating
        // that the task was not removed during the stop
        eventually {
          taskExecutionCounter.get() should be > totalTaskExecutions
        }
      }
    }

    describe("#addTask") {
      it("should add a new task at the end of the queue") {
        val loopingTaskRunner = new LoopingTaskRunner(1)
        val progressCounter = new AtomicInteger(0)
        val executedTask = new AtomicBoolean(false)

        /**
         * Waits for the counter to equal the specific value before finishing.
         * @param value The value to match
         */
        def waitForValue(value: Int) =
          while (value > progressCounter.get()) { Thread.sleep(1) }

        /**
         * Waits for the counter to equal the specific value and then marks
         * the task as executed.
         * @param value The value to match
         */
        def waitForValueAndThenMarkTrue(value: Int) = {
          waitForValue(value)
          executedTask.set(true)
        }

        // Queue up tasks to demonstrate the order on the queue
        loopingTaskRunner.addTask(waitForValue(1))
        loopingTaskRunner.addTask(waitForValueAndThenMarkTrue(2))
        loopingTaskRunner.addTask(waitForValue(3))

        // Start processing our tasks
        loopingTaskRunner.start()

        // Task should not have been executed yet
        executedTask.get() should be (false)

        // Move past our task in progress blocker
        progressCounter.set(3)

        // The task should now have been executed
        eventually {
          executedTask.get() should be (true)
        }
      }
    }

    describe("#removeTask") {
      it("should remove a task such that it is no longer executed") {
        val taskExecutionCounter = new AtomicInteger(0)
        val executedRemovedTask = new AtomicBoolean(false)

        // Add an actual task to be processed
        loopingTaskRunner.addTask(taskExecutionCounter.incrementAndGet())

        // Add a task that will be removed
        val taskToRemove =
          loopingTaskRunner.addTask(executedRemovedTask.set(true))

        // Remove the task
        loopingTaskRunner.removeTask(taskToRemove)

        // Start processing tasks
        loopingTaskRunner.start()

        // Verify that the removed task is never triggered while the other
        // task is executed more than once
        eventually {
          taskExecutionCounter.get() should be > 1
          executedRemovedTask.get() should be (false)
        }
      }
    }
  }
}
