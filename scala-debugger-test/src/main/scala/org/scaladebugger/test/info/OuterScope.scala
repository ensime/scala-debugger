package org.scaladebugger.test.info

/**
 * Provides test of expanding scope ($outer) for Scala profiles.
 *
 * @note Should have a class name of org.scaladebugger.test.info.OuterScope
 */
object OuterScope extends App {
  var x = 0

  while (true) {
    val newValue = if (x != 0) 0 else 1

    x = newValue

    // Release resources and make possible to interrupt
    Thread.sleep(1)
  }
}
