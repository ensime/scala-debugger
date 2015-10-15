package org.senkbeil.debugger.test

import scala.language.reflectiveCalls

object Main extends App {
  val x = 3
  var y = 4
  val x123 = "huh?"

  def runMe(x: Int = 3) = println(x)

  val myClass = new MyClass((x) => (z) => x + z) {
    def anotherMethod = {
      val something = 1
      something + "asdf"
    }
  }

  while (true) {
    val z = x + y

    myClass.process(3)
    myClass.anotherMethod

    val func = (x: Int, y: Int) => {
      println(s"Adding $x + $y")
      x + y
    }

    println("Running " + runMe())
    Thread.sleep(1000)
    println("Past sleep!")

    println(z)
  }

  y = 5
  runMe()
}

class MyClass(www: Int => Int => Int) {
  def process(x: Int) = www(x)(x)
}