package org.scaladebugger.tool.frontend.history

/**
 * Represents the interface for storing and loading terminal history.
 */
trait HistoryManager {
  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  def writeLine(line: String): Unit

  /**
   * Adds multiple lines to the current history and updates the persistent
   * history.
   *
   * @param lines The lines to add
   */
  def writeLines(lines: Seq[String]): Unit = lines.foreach(writeLine)

  /**
   * Returns the collection of lines stored in history.
   *
   * @return The collection of lines
   */
  def lines: Seq[String]

  /**
   * Returns the maximum number of lines that will be kept in history.
   *
   * @return The maximum number of lines, or -1 if there is no limit
   */
  def maxLines: Int

  /**
   * Clears the internal history, but leaves the persistent copy.
   */
  def reset(): Unit

  /**
   * Destroys the persistent copy of the history, but leaves the internal
   * history.
   */
  def destroy(): Unit

  /**
   * Clears both the internal history as well as the persistent copy.
   */
  def resetAndDestroy(): Unit = {
    reset()
    destroy()
  }
}
