package org.scaladebugger.tool.frontend.history
import java.io.{File, FileWriter, PrintWriter}
import java.net.URI
import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.mutable
import scala.io.Source
import scala.util.Try
import scala.collection.JavaConverters._

object FileHistoryManager {
  /**
   * Creates a new file history manager that uses the specified URI as the
   * persistence layer for its history. If the file exists, all existing lines
   * of history will be loaded into the manager.
   *
   * @param uri The uri to serve as the source of the history
   * @param maxLines The maximum number of lines to keep in history
   * @return Some(FileHistoryManager) if successfully loaded a file,
   *         otherwise None
   */
  def using(uri: URI, maxLines: Int = -1): Try[FileHistoryManager] = Try {
    val f = new File(uri) // Can throw IllegalArgumentException

    val rawLines = Try(Source.fromFile(f).getLines()).getOrElse(Nil)
      .map(_.trim).filter(_.nonEmpty).toSeq

    // If we have more lines in the file than desired, limit ourselves to the
    // last N lines
    val lines =
      if (maxLines > 0 && rawLines.size > maxLines) rawLines.takeRight(maxLines)
      else rawLines

    new FileHistoryManager(f, maxLines = maxLines, initialLines = lines)
  }
}

/**
 * Represents a history manager that stores and loads history via local files.
 *
 * @param f The file used as the source of the history
 * @param maxLines The maximum number of lines to keep in history
 * @param initialLines The initial lines to use as the start of the history
 */
class FileHistoryManager private(
  private val f: File,
  val maxLines: Int,
  private val initialLines: Seq[String] = Nil
) extends HistoryManager {
  @volatile private var _writer: PrintWriter = newPrintWriter()

  private type HistoryQueue = java.util.Queue[String]
  private val _history: HistoryQueue =
    new ConcurrentLinkedQueue[String](initialLines.asJava)

  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = withHistoryAndWriter { (h, w) =>
    // If no line should be stored, exit immediately
    if (maxLines == 0) return

    // Add line to local history
    h.add(line)

    // If over limit, remove the oldest line and rewrite history
    if (maxLines > 0 && h.size > maxLines) {
      h.poll() // Remove oldest element

      // Update file copy by clearing old file and writing new history
      destroy()

      // Batch all of the lines and then write
      // NOTE: Need to use private _writer since the old writer is now closed
      _writer.synchronized {
        lines.foreach(l =>
          _writer.write(l + System.getProperty("line.separator"))
        )
        _writer.flush()
      }

    // Otherwise, write to file immediately
    } else {
      w.println(line.trim)
    }
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
  override def lines: Seq[String] = withHistory(_.asScala.toSeq)

  /**
   * Clears the internal history.
   */
  override def reset(): Unit = withHistory(_.clear())

  /**
   * Destroys the persistent copy of the history, but leaves the internal
   * history.
   */
  override def destroy(): Unit = withWriter { (w) =>
    w.close()
    f.delete()
    _writer = newPrintWriter()
  }

  protected def newPrintWriter(): PrintWriter = {
    new PrintWriter(new FileWriter(f), true)
  }

  private def withHistoryAndWriter[T](f: (HistoryQueue, PrintWriter) => T): T =
    withHistory(h => withWriter(w => f(h, w)))

  private def withHistory[T](f: HistoryQueue => T): T = _history.synchronized {
    f(_history)
  }

  private def withWriter[T](f: PrintWriter => T): T = _writer.synchronized {
    f(_writer)
  }
}
