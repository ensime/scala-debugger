package org.senkbeil.debugger.test.misc

/**
 * Provides an example of a main entrypoint using Scala's App instead of a
 * main method.
 *
 * @note Should have a class name of org.senkbeil.test.msic.MainUsingApp
 * @note Should have breakpoint lines on 10 and 11
 */
object MainUsingApp extends App {
  val x = 3

  println(x)

  // Needed for tests to examine JVM without needing to set breakpoints
  while (!Thread.interrupted()) { Thread.sleep(1) }
}
