package org.scaladebugger.docs

/**
 * Represents a simplistic logger.
 */
class Logger(private val klass: Class[_]) {
  /**
   * Logs the text content.
   *
   * @param text The text to log
   */
  def log(text: String): Unit = {
    import java.util.Calendar
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    val timestamp = format.format(Calendar.getInstance().getTime)
    println(s"[$timestamp] $text")
  }
}
