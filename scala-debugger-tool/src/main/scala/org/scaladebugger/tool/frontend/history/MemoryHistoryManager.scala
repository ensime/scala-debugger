package org.scaladebugger.tool.frontend.history

import java.io.{File, FileWriter, PrintWriter}
import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.JavaConverters._

object MemoryHistoryManager {
  /**
   * Creates a new instance of the memory history manager.
   *
   * @param maxLines The maximum number of lines to keep in history
   * @param initialLines The initial lines to use as the start of the history
   * @return The new instance of the memory history manager
   */
  def newInstance(
    maxLines: Int = -1,
    initialLines: Seq[String] = Nil
  ): MemoryHistoryManager = new MemoryHistoryManager(maxLines, initialLines)
}

/**
 * Represents a history manager that stores and loads history using memory.
 *
 * @param maxLines The maximum number of lines to keep in history
 * @param initialLines The initial lines to use as the start of the history
 */
class MemoryHistoryManager private[history](
  val maxLines: Int,
  private val initialLines: Seq[String] = Nil
) extends HistoryManager {
  private type HistoryQueue = java.util.Queue[String]
  private val _history: HistoryQueue = newHistoryQueue()

  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = withHistory { h =>
    // If no line should be stored, exit immediately
    if (maxLines == 0) return

    // Add line to local history
    h.add(line.trim)

    // If over limit, remove the oldest line
    if (maxLines > 0 && h.size > maxLines) h.poll()
  }

  /**
   * Returns the collection of lines stored in history.
   *
   * @return The collection of lines
   */
  override def lines: Seq[String] = withHistory(_.asScala.toSeq.reverse)

  /**
   * Destroys the history in memory.
   */
  override def destroy(): Unit = withHistory(_.clear())

  /**
   * Creates a new queue used for internal history storage.
   *
   * @return The new queue instance
   */
  protected def newHistoryQueue(): HistoryQueue =
    new ConcurrentLinkedQueue[String](initialLines.asJava)

  /**
   * Evaluates a given function by passing in the current history. Synchronizes
   * against the current history.
   *
   * @param f The function to evaluate
   * @tparam T The return type from the function
   * @return The result of evaluating the function
   */
  protected def withHistory[T](f: HistoryQueue => T): T = _history.synchronized {
    f(_history)
  }
}
