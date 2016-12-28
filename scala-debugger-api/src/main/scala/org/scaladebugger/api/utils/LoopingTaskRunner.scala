package org.scaladebugger.api.utils

import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import scala.util.Try

import LoopingTaskRunner._

/**
 * Contains defaults for the looping task runner.
 */
object LoopingTaskRunner {
  /** Default initial workers is equal to number of available processors */
  val DefaultInitialWorkers: Int = Runtime.getRuntime.availableProcessors()

  /** Default maximum wait time is 100 milliseconds */
  val DefaultMaxTaskWaitTime: (Long, TimeUnit) = (100L, TimeUnit.MILLISECONDS)
}

/**
 * Represents a queue of tasks that will be executed infinitely in order
 * until removed.
 *
 * @param initialWorkers The total number of works to use for this runner on
 *                       startup (more can be added or removed)
 * @param maxTaskWaitTime The maximum time to wait for a task to be pulled off
 *                        of the queue before allowing other tasks to be run
 */
class LoopingTaskRunner(
  private val initialWorkers: Int = DefaultInitialWorkers,
  private val maxTaskWaitTime: (Long, TimeUnit) = DefaultMaxTaskWaitTime
) {
  type TaskId = String

  /**
   * Represents a task that will execute the next task on the provided queue
   * and add it back to the end of the queue when finished.
   *
   * @param taskQueue The queue containing the ids of the tasks to run
   * @param taskMap The mapping of task ids to associated runnable tasks
   */
  private class LoopingTask(
    private val taskQueue: util.concurrent.BlockingQueue[TaskId],
    private val taskMap: util.Map[TaskId, Runnable]
  ) extends Runnable {
    override def run(): Unit = {
      // Update tracking information to reflect an active worker
      currentActiveWorkers.incrementAndGet()

      // Determine the next task to execute (wait for a maximum time duration)
      val taskId = nextTaskId()

      // If there is a new task, perform the operation
      taskId.foreach(executeTask)

      // Start next task once this is free (suppress exceptions in the
      // situation that this runner has been stopped)
      //
      // NOTE: Do not add this runner back on our queue if we want to decrease
      //       the total number of workers via the desired total workers
      if (currentActiveWorkers.decrementAndGet() < desiredTotalWorkers.get()) {
        Try(runNextTask())
      }
    }

    /**
     * Retrieves the id of the next task to execute.
     *
     * @return Some id if there is a new task to execute, otherwise None
     */
    protected def nextTaskId(): Option[TaskId] = Option(taskQueue.poll(
      maxTaskWaitTime._1,
      maxTaskWaitTime._2
    ))

    /**
     * Retrieves and executes the next task, placing it back on the queue
     * once finished.
     *
     * @param taskId The id of the task to execute
     */
    protected def executeTask(taskId: TaskId): Unit = {
      // Retrieve and execute the next task
      val tryTask = Try(taskMap.get(taskId))
      tryTask.foreach(task => Try(task.run()))

      // Task finished, so add back to end of our queue
      // NOTE: Only do so if the map knows about our task (allows removal)
      if (tryTask.isSuccess) taskQueue.put(taskId)
    }
  }

  /** Represents the desired number of workers processing and on queue */
  private val desiredTotalWorkers = new AtomicInteger(0)

  /** Represents the total number of workers processing tasks */
  private val currentActiveWorkers = new AtomicInteger(0)

  /** Contains the ids of tasks to be executed (in order). */
  private val taskQueue = new LinkedBlockingQueue[TaskId]()

  /** Contains mapping of task ids to task implementations. */
  private val taskMap = new ConcurrentHashMap[TaskId, Runnable]()

  /** Represents the executors used to execute the tasks. */
  @volatile private var executorService: Option[ExecutorService] = None

  /**
   * Indicates whether or not the task runner is processing tasks.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = executorService.nonEmpty

  /**
   * Executing begins the process of executing queued up tasks.
   */
  def start(): Unit = {
    assert(!isRunning, "Runner already started!")

    // Create our thread pool executor to process tasks
    executorService = Some(newExecutorService())

    // Start X tasks to be run
    setDesiredTotalWorkers(initialWorkers)
  }

  /**
   * Prevents the runner from executing any more tasks.
   *
   * @param removeAllTasks If true, removes all tasks after being stopped
   */
  def stop(removeAllTasks: Boolean = true): Unit = {
    assert(isRunning, "Runner not started!")

    setDesiredTotalWorkers(0)
    executorService.foreach(es => {
      es.shutdown()
      es.awaitTermination(10, TimeUnit.SECONDS)
    })
    executorService = None

    if (removeAllTasks) {
      taskQueue.clear()
      taskMap.clear()
    }
  }

  /**
   * Sets the desired total number of workers to eventually be achieved by
   * the task runner.
   *
   * @param value The new desired total number of workers
   */
  def setDesiredTotalWorkers(value: Int): Unit = {
    // Determine if this is an increase or decrease in workers
    val delta = value - desiredTotalWorkers.getAndSet(value)

    // If an increase, we need to spawn new tasks to increase our workers
    if (delta > 0) (1 to delta).foreach(_ => runNextTask())
  }

  /**
   * Retrieves the current desired total number of workers.
   *
   * @return The desired total number of workers
   */
  def getDesiredTotalWorkers: Int = desiredTotalWorkers.get()

  /**
   * Retrieves the total actively-running workers.
   *
   * @return The total active workers at this point in time
   */
  def getCurrentActiveWorkers: Int = currentActiveWorkers.get()

  /**
   * Adds a task to be executed repeatedly (in a queue with other tasks).
   *
   * @param task The task to add
   * @tparam T The return type of the task
   *
   * @return The id of the queued task
   */
  def addTask[T](task: => T): TaskId = {
    val taskId = java.util.UUID.randomUUID().toString

    // Add the task to our lookup table, and then queue it up for processing
    taskMap.put(taskId, new Runnable {
      override def run(): Unit = task
    })
    taskQueue.put(taskId)

    taskId
  }

  /**
   * Removes a task from the repeated execution.
   *
   * @param taskId The id of the task to remove
   *
   * @return Task implementation that was removed
   */
  def removeTask(taskId: TaskId): Runnable = {
    taskQueue.remove(taskId)
    taskMap.remove(taskId)
  }

  /**
   * Creates a new executor service for use by the looping task runner.
   *
   * @return The new executor service instance
   */
  protected def newExecutorService(): ExecutorService = {
    // TODO: Replace cached thread pool with more optimized thread pool
    //       executor that scales better for large number of tasks
    Executors.newCachedThreadPool()
  }

  /**
   * Executes next available task.
   */
  protected def runNextTask(): Unit =
    executorService.foreach(_.execute(newLoopingTask()))

  /**
   * Creates a new looping task to be executed.
   */
  protected def newLoopingTask(): Runnable = new LoopingTask(taskQueue, taskMap)
}
