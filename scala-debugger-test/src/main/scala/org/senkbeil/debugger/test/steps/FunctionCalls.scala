package org.senkbeil.debugger.test.steps

import org.senkbeil.debugger.test.helpers.Stubs._

/**
  * Provides test of performing basic step in/out/over in Scala situations
  * involving invoking functions.
  *
  * @note Should have a class name of org.senkbeil.test.steps.FunctionCalls
  */
object FunctionCalls {
  val objectFunction1 = (x: Int, y: Int) => x + y
  val objectFunction2 = new Function2[Int, Int, Int] {
    override def apply(x: Int, y: Int): Int = x + y
  }
  val objectFunction3 = new Function2[Int, Int, Int] {
    override def apply(x: Int, y: Int): Int = {
      val x2 = x + 2
      val y2 = y + 2

      x2 + y2
    }
  }

  def main(args: Array[String]) = {
    val methodFunction1 = (x: Int, y: Int) => x + y
    val methodFunction2 = new Function2[Int, Int, Int] {
      override def apply(x: Int, y: Int): Int = x + y
    }
    val methodFunction3 = new Function2[Int, Int, Int] {
      override def apply(x: Int, y: Int): Int = {
        val x2 = x + 2
        val y2 = y + 2

        x2 + y2
      }
    }

    objectFunction1(1, 2)
    objectFunction2(3, 4)
    objectFunction3(5, 6)

    methodFunction1(7, 8)
    methodFunction2(9, 10)
    methodFunction3(11, 12)

    val innerClass = new InnerClass
    innerClass.innerFunction(13, 14)

    ((x: Int, y: Int) => x + y)(15, 16)

    noop(None)
  }

  class InnerClass {
    val innerFunction = (x: Int, y: Int) => {
      val x2 = x + 2
      val y2 = y + 2
      x2 + y2
    }
  }
}
