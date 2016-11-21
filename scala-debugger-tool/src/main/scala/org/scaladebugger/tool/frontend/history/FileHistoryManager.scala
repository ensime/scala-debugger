package org.scaladebugger.tool.frontend.history
import java.io.{File, FileWriter, PrintWriter}
import java.net.URI

import scala.io.Source
import scala.collection.mutable.ListBuffer
import scala.util.Try

object FileHistoryManager {
  /**
   * Creates a new file history manager that uses the specified URI as the
   * persistence layer for its history. If the file exists, all existing lines
   * of history will be loaded into the manager.
   *
   * @param uri The uri to serve as the source of the history
   * @return Some(FileHistoryManager) if successfully loaded a file,
   *         otherwise None
   */
  def using(uri: URI): Try[FileHistoryManager] = Try {
    val f = new File(uri) // Can throw IllegalArgumentException

    val lines = Try(Source.fromFile(f).getLines()).getOrElse(Nil)
      .map(_.trim).filter(_.nonEmpty).toSeq

    new FileHistoryManager(f, initialLines = lines)
  }
}

/**
 * Represents a history manager that stores and loads history via local files.
 *
 * @param f The file used as the source of the history
 * @param initialLines The initial lines to use as the start of the history
 */
class FileHistoryManager private(
  private val f: File,
  private val initialLines: Seq[String] = Nil
) extends HistoryManager {
  @volatile private var _writer: PrintWriter = newPrintWriter()

  private type HistoryList = ListBuffer[String]
  private val _history: HistoryList = ListBuffer(initialLines: _*)

  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = withHistoryAndWriter { (h, w) =>
    h += line
    w.println(line.trim)
  }

  /**
   * Adds multiple lines to the current history and updates the persistent
   * history.
   *
   * @param lines The lines to add
   */
  override def writeLines(lines: Seq[String]): Unit = lines.foreach(writeLine)

  /**
   * Returns the collection of lines stored in history.
   *
   * @return The collection of lines
   */
  override def lines: Seq[String] = withHistory(h => h)

  /**
   * Clears the internal history.
   */
  override def reset(): Unit = withHistory(_.clear())

  /**
   * Destroys the history, including the persistent copy.
   */
  override def destroy(): Unit = withHistoryAndWriter { (h, w) =>
    w.close()
    f.delete()
    _writer = newPrintWriter()
    h.clear()
  }

  protected def newPrintWriter(): PrintWriter = {
    new PrintWriter(new FileWriter(f), true)
  }

  private def withHistoryAndWriter[T](f: (HistoryList, PrintWriter) => T): T =
    withHistory(h => withWriter(w => f(h, w)))

  private def withHistory[T](f: HistoryList => T): T = _history.synchronized {
    f(_history)
  }

  private def withWriter[T](f: PrintWriter => T): T = _writer.synchronized {
    f(_writer)
  }
}
