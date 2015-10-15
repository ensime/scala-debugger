package org.senkbeil.debugger.test.methods

/**
 * Used to verify the MethodEntry manager.
 */
object MethodEntry extends App {
  def objectMethod() = {
    def innerMethod() = {}

    innerMethod()
  }

  val a = objectMethod()
  val b = new TestClass().someOtherMethod()
  val c = new TestClass().testMethod()

  while (true) { Thread.sleep(1000) }
}

class TestClass {
  def testMethod() = {}
  def someOtherMethod() = {}
}
