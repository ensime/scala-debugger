package org.senkbeil.debugger.test.steps

import org.senkbeil.debugger.test.helpers.Stubs._

/**
 * Provides test of performing basic step in/out/over in Scala situations
 * involving inheritance.
 *
 * @note Should have a class name of org.senkbeil.test.steps.Inheritance
 *
 * @note Lifted from Ensime, which in turn was lifted from Scala IDE.
 */
object Inheritance {
  def main(args: Array[String]) = {
    bridges(); noop(None)
  }

  def bridges() {
    val c: Base[Int] = new Concrete

    c.base(10)
    println(c.base(10))

    2 + c.base(10)

    noop(None)
  }

  class Base[T] {
    def base(x: T): Int = 0
  }

  class Concrete extends Base[Int] {
    override def base(x: Int): Int = {
      println(x)
      x
    }
  }
}
