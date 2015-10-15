package org.senkbeil.debugger.test.breakpoints

/**
 * Provides test of iterating through a while loop.
 *
 * @note Should have a class name of
 *       org.senkbeil.test.breakpoints.ForComprehension
 */
object ForComprehension {
  def main(args: Array[String]) = {
    var count = 0

    for (i <- 1 to 10) {
      count = i // Verify that this is reached via breakpoint 10 times
    }

    for (i <- 1 to 10) {
      count = i + 1 // Verify that this is reached via breakpoint 10 times
    }
  }
}
