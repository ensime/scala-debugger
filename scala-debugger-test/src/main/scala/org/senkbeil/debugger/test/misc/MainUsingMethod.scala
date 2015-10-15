package org.senkbeil.debugger.test.misc

/**
 * Provides an example of a main entrypoint using Scala's App instead of a
 * main method.
 *
 * @note Should have a class name of org.senkbeil.test.misc.MainUsingMethod
 * @note Should have breakpoint lines on 10, 13, and 16
 */
object MainUsingMethod {
  def main(args: Array[String]) = {
    // Print out our arguments
    args.foreach(println)

    // Needed for tests to examine JVM without needing to set breakpoints
    while (!Thread.interrupted()) { Thread.sleep(1) }
  }
}
