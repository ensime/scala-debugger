package org.senkbeil.debugger.test.methods

/**
 * Used to verify the MethodEntry manager.
 */
object MethodExit extends App {
  def objectMethod() = {
    def innerMethod() = {
      val x = 1
      val y = 2
      x + y
    }

    innerMethod()
  }

  val a = objectMethod()
  val b = new MethodExitTestClass().someOtherMethod()
  val c = new MethodExitTestClass().testMethod()

  while (true) { Thread.sleep(1000) }
}

class MethodExitTestClass {
  def testMethod() = {
    val x = 1
    val y = 2
    x + y
  }
  def someOtherMethod() = {}
}
