package org.senkbeil.debugger.test.exceptions

/**
 * Used to test exception reporting in uncaught situations.
 */
object InsideFunctionalTryException extends App {
  // Used to load the exception class into the JVM,
  // otherwise it cannot be tracked
  val customException = new CustomException
  val x = 1 + 1 // Breakpoint this line to prepare without passing exceptions

  // Scala functional try
  scala.util.Try(throw new CustomException)

  while (true) { Thread.sleep(1000) }
}
