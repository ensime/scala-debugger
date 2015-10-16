package org.senkbeil.debugger.test.exceptions

/**
 * Used to test exception reporting in uncaught situations.
 */
object OutsideTryException extends App {
  // Used to load the exception class into the JVM,
  // otherwise it cannot be tracked
  val customException = new CustomException
  val x = 1 + 1 // Breakpoint this line to prepare without passing exceptions

  // Unexposed exception
  throw new CustomException

  while (true) { Thread.sleep(1000) }
}
