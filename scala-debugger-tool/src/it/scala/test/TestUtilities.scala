package test

import org.scaladebugger.api.utils.Logging
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.{Assertion, Matchers}

/**
 * Contains helper methods for testing.
 */
trait TestUtilities { this: Logging with Matchers =>
  /**
   * Executes the block of code and logs the time taken to evaluate it.
   *
   * @param block The block of code to execute
   * @tparam T The return type of the block of code
   *
   * @return The value returned from the block of code
   */
  def logTimeTaken[T](block: => T): T = {
    val startTime = System.currentTimeMillis()

    try {
      block
    } finally {
      val finalTime = System.currentTimeMillis() - startTime
      logger.info(s"Time taken: ${finalTime / 1000.0}s")
    }
  }

  /**
   * Retrieves the next line of output from the virtual terminal.
   *
   * @param vt The virtual terminal whose output to retrieve
   * @return Some line of text if found in time, otherwise None
   */
  def nextLine(vt: VirtualTerminal): Option[String] = {
    val waitTime = Constants.NextOutputLineTimeout.millisPart
    vt.nextOutputLine(waitTime = waitTime)
  }

  /**
   * Validates the text against the next line of output.
   *
   * @param vt The virtual terminal whose output to retrieve
   * @param text The text to validate against
   * @param success Optional function used to evaluate a success, taking
   *                the text as the first argument and output line as second
   * @param fail Optional function used to report a failure
   * @return Scalatest assertion result
   */
  def validateNextLine(
    vt: VirtualTerminal,
    text: String,
    success: (String, String) => Assertion = (text, line) => text should be (line),
    fail: String => Assertion = fail(_: String)
  ): Assertion = {
    import scala.reflect.runtime.universe._
    val t = Literal(Constant(text)).toString
    nextLine(vt) match {
      case Some(line) =>
        val l = Literal(Constant(line)).toString
        logger.debug(s"Checking '$l' against '$t'")
        println(s"${this.getClass.getName}: Checking '$l' against '$t'")
        success(text, line)
      case None =>
        fail(s"Unable to find desired line in time: '$t'")
    }
  }
}
