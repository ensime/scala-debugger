package org.scaladebugger.test.info

import org.scaladebugger.test.helpers.Stubs._

/**
 * Provides test of examining variable values and setting variable values.
 *
 * @note This was lifted from ensime-server, which in turn lifted it from
 *       the Scala IDE project! Modified to accommodate additional testing.
 *
 * @note Should have a class name of org.scaladebugger.test.info.Variables
 */
object Variables {
  var z1 = 1.toByte
  var z2 = "something"
  val z3 = null

  def main(args: Array[String]) {
    val a = true
    val b = 'c'
    val c = 3.asInstanceOf[Short]
    val d = 4
    val e = 5L
    val f = 1.0f
    var g = 2.0
    var h = "test"
    val i = Array(1, 2, 3)
    val j = List(4, 5, 6)
    val k = Array(One("one"), 1, true)
    val l = NullToString

    noop(None)

    val m = z1 + c + d + e + f + g
    val n = i.sum + j.sum

    noop(None)

    val o = (() => c + d)()
    val p = (() => {
      z2 + b + h
    })()

    noop(None)
  }

  case class One(s: String) { override def toString = s }

  object NullToString { override def toString = null }
}

