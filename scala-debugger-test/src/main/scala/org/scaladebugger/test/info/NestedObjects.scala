package org.scaladebugger.test.info

/**
 * Provides test of examining variable values that include nested objects.
 *
 * @note Should have a class name of org.scaladebugger.test.info.NestedObjects
 */
object NestedObjects {
  case class Data(x: Int, y: String)
  class Container {
    val immutableData: Data = Data(3, "immutable")
    var mutableData: Data = Data(999, "mutable")
  }

  def main(args: Array[String]) = {
    val container = new Container
    val imd = container.immutableData
    val md = container.mutableData

    println(s"Container: $container")
    println(s"Immutable Data: $imd")
    println(s"Mutable Data: $md")
  }
}
