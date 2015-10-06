package org.senkbeil.debugger.events

import java.util.concurrent.{ExecutorService, Executors, ConcurrentHashMap, LinkedBlockingQueue}

import scala.reflect.ClassTag
import scala.util.Try

/**
 * Represents a queue of tasks that will be executed infinitely in order
 * until removed.
 *
 * @param maxWorkers The total number of works to use for this runner,
 *                   defaulting to the total number of available processors
 */
class LoopingTaskRunner(
  private val maxWorkers: Int = Runtime.getRuntime.availableProcessors()
) {
  type TaskId = String

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

    // Create our thread pool with X workers to process tasks
    executorService = Some(Executors.newFixedThreadPool(maxWorkers))

    // Start X tasks to be run
    (1 to maxWorkers).foreach(_ => runNextTask())
  }

  /**
   * Prevents the runner from executing any more tasks.
   *
   * @param removeAllTasks If true, removes all tasks after being stopped
   */
  def stop(removeAllTasks: Boolean = true): Unit = {
    assert(isRunning, "Runner not started!")

    executorService.get.shutdown()
    executorService = None

    if (removeAllTasks) {
      taskQueue.clear()
      taskMap.clear()
    }
  }

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
   * Executes next available task.
   */
  protected def runNextTask(): Unit =
    executorService.foreach(_.execute(new Runnable {
      override def run(): Unit = {
        // Determine the next task to execute (waits if no task available)
        val taskId = taskQueue.take()

        // Retrieve and execute the next task
        val tryTask = Try(taskMap.get(taskId))
        tryTask.foreach(task => Try(task.run()))

        // Task finished, so add back to end of our queue
        // NOTE: Only do so if the map knows about our task (allows removal)
        if (tryTask.isSuccess) taskQueue.put(taskId)

        // Start next task once this is free (suppress exceptions in the
        // situation that this runner has been stopped)
        Try(runNextTask())
      }
    }))
}
