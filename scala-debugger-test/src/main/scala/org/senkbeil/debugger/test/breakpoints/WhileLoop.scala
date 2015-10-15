package org.senkbeil.debugger.test.breakpoints

/**
 * Provides test of iterating through a while loop.
 *
 * @note Should have a class name of org.senkbeil.test.breakpoints.WhileLoop
 */
object WhileLoop {
  def main(args: Array[String]) = {
    var count = 0

    while (count < 10) {
      count += 1 // Verify that this is reached via breakpoint 10 times
    }

    while (count > 0) {
      count -= 1 // Verify that this is reached via breakpoint 10 times
    }
  }
}
