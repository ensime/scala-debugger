package org.scaladebugger.docs

import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.docs.Logger.Session

/**
 * Represents a simplistic logger.
 *
 * @param klass The class using the logger
 */
class Logger(private val klass: Class[_]) {
  private lazy val StdOutWriter = new PrintWriter(Console.out)
  private lazy val StdErrWriter = new PrintWriter(Console.err)

  /**
   * Starts a new logger session with the specified name.
   *
   * @param name The name to associate with the logger session
   * @return The new session instance
   */
  def newSession(name: String): Logger.Session = new Session(klass, name)

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
  protected def log(printWriter: PrintWriter, text: String): Unit = {
    import java.util.Calendar
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    val timestamp = format.format(Calendar.getInstance().getTime)
    printWriter.println(s"[$timestamp] $text")
    printWriter.flush()
  }
}

object Logger {
  /** Represents a logger that does nothing. */
  class Noop private[Logger] extends Logger(null) {
    override protected def log(
      printWriter: PrintWriter,
      text: String
    ): Unit = {}
  }

  /** Represents a logger that does nothing. */
  lazy val Noop: Logger = new Noop

  /**
   * Represents a logger session, used to
   *
   * @param klass The class using the logger
   * @param name The name associated with the session
   */
  class Session private[Logger] (
    private val klass: Class[_],
    val name: String
  ) extends Logger(klass) {
    private val isInitialized: AtomicBoolean = new AtomicBoolean(false)

    /**
     * Initializes the session.
     *
     * @return The initialized logger
     */
    def init(): Session = {
      log(s"(( $name ))")
      isInitialized.set(true)
      this
    }

    /**
     * Logs the text content.
     *
     * @param printWriter The writer to use when logging
     * @param text        The text to log
     */
    override protected def log(
      printWriter: PrintWriter,
      text: String
    ): Unit = super.log(
      printWriter,
      (if (isInitialized.get()) "\t" else "") + text
    )
  }
}
