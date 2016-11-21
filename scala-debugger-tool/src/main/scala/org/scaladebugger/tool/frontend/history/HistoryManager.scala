package org.scaladebugger.tool.frontend.history

import java.net.URI

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
  def writeLines(lines: Seq[String]): Unit

  /**
   * Returns the collection of lines stored in history.
   *
   * @return The collection of lines
   */
  def lines: Seq[String]

  /**
   * Clears the internal history, but leaves the persistent copy.
   */
  def reset(): Unit

  /**
   * Destroys the history, including the persistent copy.
   */
  def destroy(): Unit
}
