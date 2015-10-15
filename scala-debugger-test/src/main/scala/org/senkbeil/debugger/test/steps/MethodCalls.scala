package org.senkbeil.debugger.test.steps

import org.senkbeil.debugger.test.helpers.Stubs._

/**
 * Provides test of performing basic step in/out/over in Scala situations
 * involving invoking methods.
 *
 * @note Should have a class name of org.senkbeil.test.steps.MethodCalls
 */
object MethodCalls {
  def objectMethod1 = (x: Int, y: Int) => x + y
  def objectMethod2(x: Int, y: Int) =  x + y
  def objectMethod3(x: Int, y: Int) = {
    val x2 = x + 2
    val y2 = y + 2

    x2 + y2
  }

  def main(args: Array[String]) = {
    def methodMethod1 = (x: Int, y: Int) => x + y
    def methodMethod2(x: Int, y: Int) =  x + y
    def methodMethod3(x: Int, y: Int) = {
      val x2 = x + 2
      val y2 = y + 2

      x2 + y2
    }

    noop(None)

    objectMethod1(1, 2)
    objectMethod2(3, 4)
    objectMethod3(5, 6)

    methodMethod1(7, 8)
    methodMethod2(9, 10)
    methodMethod3(11, 12)

    val innerClass = new InnerClass()
    innerClass.innerMethod(13, 14)

    noop(None)
  }

  class InnerClass {
    def innerMethod(x: Int, y: Int) = {
      val x2 = x + 2
      val y2 = y + 2
      x2 + y2
    }
  }
}
