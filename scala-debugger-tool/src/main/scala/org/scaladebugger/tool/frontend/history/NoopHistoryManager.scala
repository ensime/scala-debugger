package org.scaladebugger.tool.frontend.history

/**
 * Represents a history manager that performs no-ops for all operations.
 * Essentially, this manager represents the /dev/null of history managers.
 */
class NoopHistoryManager private extends HistoryManager {
  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = {}

  /**
   * Returns the maximum number of lines that will be kept in history.
   *
   * @return The maximum number of lines, or -1 if there is no limit
   */
  override def maxLines: Int = -1

  /**
   * Returns the collection of lines stored in history.
   *
   * @return The collection of lines
   */
  override def lines: Seq[String] = Nil

  /**
   * Destroys the internal and persistent history.
   */
  override def destroy(): Unit = {}
}

object NoopHistoryManager {
  /** Represents the singleton instance of the no-op history manager. */
  lazy val Instance = newInstance()

  /** Creates a new instance of the no-op history manager. */
  private def newInstance(): NoopHistoryManager = new NoopHistoryManager
}
