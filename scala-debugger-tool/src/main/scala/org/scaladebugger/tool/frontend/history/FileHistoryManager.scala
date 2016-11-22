package org.scaladebugger.tool.frontend.history
import java.io.{File, FileWriter, PrintWriter}
import java.net.URI

import scala.io.Source
import scala.util.Try
import scala.collection.JavaConverters._

object FileHistoryManager {
  /**
   * Creates a new file history manager that uses the specified file as the
   * persistence layer for its history. If the file exists, all existing lines
   * of history will be loaded into the manager.
   *
   * @param f The file to serve as the source of the history
   * @param maxLines The maximum number of lines to keep in history
   * @param loadLines Custom method to load lines from a file
   * @param newPrintWriter Custom method to generate a print writer from a file
   * @return Some(FileHistoryManager) if successfully loaded a file,
   *         otherwise None
   */
  def newInstance(
    f: File,
    maxLines: Int = -1,
    loadLines: File => Seq[String] = FileHistoryManager.loadLines,
    newPrintWriter: File => PrintWriter = FileHistoryManager.newPrintWriter
  ): FileHistoryManager = {
    val rawLines = loadLines(f)

    // If we have more lines in the file than desired, limit ourselves to the
    // last N lines
    val lines =
      if (maxLines > 0 && rawLines.size > maxLines) rawLines.takeRight(maxLines)
      else rawLines

    new FileHistoryManager(
      f = f,
      maxLines = maxLines,
      initialLines = lines,
      newPrintWriter = newPrintWriter
    )
  }

  /**
   * Creates a new print writer using the provided file.
   *
   * @param f The file to wrap in a print writer
   * @return The new print writer with auto-flush enabled and appending
   */
  private def newPrintWriter(f: File): PrintWriter = {
    new PrintWriter(new FileWriter(f, true), true)
  }

  /**
   * Loads lines from a file, skipping empty lines.
   *
   * @param f The file whose lines to load
   * @return The lines of text from the file
   */
  private def loadLines(f: File): Seq[String] = {
    Try(Source.fromFile(f).getLines()).getOrElse(Nil)
      .map(_.trim).filter(_.nonEmpty).toSeq
  }
}

/**
 * Represents a history manager that stores and loads history via local files.
 *
 * @param f The file used as the source of the history
 * @param maxLines The maximum number of lines to keep in history
 * @param initialLines The initial lines to use as the start of the history
 * @param newPrintWriter Custom method to generate a print writer from a file
 */
class FileHistoryManager private(
  private val f: File,
  override val maxLines: Int,
  private val initialLines: Seq[String] = Nil,
  private val newPrintWriter: File => PrintWriter = FileHistoryManager.newPrintWriter
) extends MemoryHistoryManager(maxLines, initialLines) {
  @volatile private var _writer: PrintWriter = newPrintWriter(f)

  /**
   * Adds a new line to the current history and updates the persistent history.
   *
   * @param line The line to add
   */
  override def writeLine(line: String): Unit = {
    // Write line in memory first
    val reachedLimit = maxLines > 0 && this.size >= maxLines
    super.writeLine(line)

    // If no line should be stored, exit immediately
    if (maxLines == 0) return

    withWriter { w =>
      // If over limit, rewrite history
      if (reachedLimit) {
        // Update file copy by clearing old file and writing new history
        destroyFile()

        // Batch all of the lines and then write
        // NOTE: Need to use private _writer since the old writer is now closed
        _writer.synchronized {
          // NOTE: Assuming that lines are proper size based on inherited
          //       memory history manager
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
   * Destroys the internal history and wipes the history file.
   */
  override def destroy(): Unit = {
    super.destroy()

    // Also wipe the external file
    destroyFile()
  }

  /**
   * Deletes the history file and creates a new writer
   * to be used for subsequent writes.
   */
  protected def destroyFile(): Unit = withWriter { w =>
    w.close()
    f.delete()
    _writer = newPrintWriter(f)
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
