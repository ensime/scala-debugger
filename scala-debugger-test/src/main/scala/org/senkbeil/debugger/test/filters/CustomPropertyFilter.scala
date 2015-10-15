package org.senkbeil.debugger.test.filters

/**
 * Used to verify the CustomPropertyFilter.
 *
 * Breakpointable lines are 9, 10, 11, 12, 13, 15.
 */
object CustomPropertyFilter extends App {
  val a = 0
  val b = 1
  val c = 2
  val d = 3
  val e = 4

  while (true) { Thread.sleep(1000) }
}
