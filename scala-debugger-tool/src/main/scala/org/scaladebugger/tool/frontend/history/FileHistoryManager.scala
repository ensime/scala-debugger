package org.scaladebugger.tool.frontend.history
import java.io.{File, FileWriter, PrintWriter}
import java.net.URI

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
  def newInstance(uri: URI, maxLines: Int = -1): Try[FileHistoryManager] = Try {
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
  override val maxLines: Int,
  private val initialLines: Seq[String] = Nil
) extends MemoryHistoryManager(maxLines, initialLines) {
  @volatile private var _writer: PrintWriter = newPrintWriter()

  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = {
    // Write line in memory first
    super.writeLine(line)

    // If no line should be stored, exit immediately
    if (maxLines == 0) return

    withWriter { w =>
      // If over limit, rewrite history
      if (maxLines > 0 && lines.size > maxLines) {
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
   * Destroys the persistent copy of the history, but leaves the internal
   * history.
   */
  override def destroy(): Unit = withWriter { (w) =>
    println("DESTROY: " + Try(throw new Throwable).failed.get.getStackTrace.mkString("\n"))
    w.close()
    f.delete()
    _writer = newPrintWriter()
  }

  /**
   * Creates a new print writer using the file associated with this manager.
   *
   * @return The new print writer with auto-flush enabled and appending
   */
  protected def newPrintWriter(): PrintWriter = {
    new PrintWriter(new FileWriter(f, true), true)
  }

  /**
   * Evaluates a given function by passing in the current writer. Synchronizes
   * against the current writer.
   *
   * @param f The function to evaluate
   * @tparam T The return type from the function
   *
   * @return The result of evaluating the function
   */
  protected def withWriter[T](f: PrintWriter => T): T = _writer.synchronized {
    f(_writer)
  }
}
