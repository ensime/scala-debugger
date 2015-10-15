package org.senkbeil.debugger.test.steps

import org.senkbeil.debugger.test.helpers.Stubs._

/**
 * Provides test of performing basic step in/out/over in Scala situations
 * involving iterations.
 *
 * @note Should have a class name of org.senkbeil.test.steps.BasicIterations
 */
object BasicIterations {
  def main(args: Array[String]) = {
    val totalIterations = 3

    // Test for comprehension
    for (x <- 1 to totalIterations) {
      noop(x)
    }

    // Test foreach
    (1 to totalIterations).foreach {
      noop
    }

    // Test map
    (1 to totalIterations).map {
      ret
    }

    // Test "reduce"
    (1 to totalIterations).foldLeft(0) { (acc, i) =>
      ret(i)
    }

    // Create a function object that loops
    val myFunction = (x: Int) => (1 to x).foreach {
      noop
    }

    // Create a method that loops
    def myMethod(x: Int) = (1 to x).foreach {
      noop
    }

    myFunction(totalIterations)

    myMethod(totalIterations)

    noop(None)
  }
}
