package org.scaladebugger.docs

import java.io.PrintWriter

/**
 * Represents a simplistic logger.
 */
class Logger(private val klass: Class[_]) {
  private lazy val StdOutWriter = new PrintWriter(Console.out)
  private lazy val StdErrWriter = new PrintWriter(Console.err)

  /**
   * Logs the text content to standard out.
   *
   * @param text The text to log
   */
  def log(text: String): Unit = log(StdOutWriter, text)

  /**
   * Logs the text content to standard err.
   *
   * @param text The text to log
   */
  def error(text: String): Unit = log(StdErrWriter, text)

  /**
   * Logs the throwable to standard err.
   *
   * @param throwable The throwable to log
   */
  def error(throwable: Throwable): Unit = {
    val errorName = throwable.getClass.getName
    val message = Option(throwable.getLocalizedMessage).getOrElse("")
    val stackTrace = throwable.getStackTrace.mkString("\n")
    log(StdErrWriter, errorName + ": " + message + ": " + stackTrace)
  }


  /**
   * Logs the text content.
   *
   * @param printWriter The writer to use when logging
   * @param text The text to log
   */
  private def log(printWriter: PrintWriter, text: String): Unit = {
    import java.util.Calendar
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    val timestamp = format.format(Calendar.getInstance().getTime)
    printWriter.println(s"[$timestamp] $text")
    printWriter.flush()
  }
}
