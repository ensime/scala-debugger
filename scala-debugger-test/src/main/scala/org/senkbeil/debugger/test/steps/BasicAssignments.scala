package org.senkbeil.debugger.test.steps

import org.senkbeil.debugger.test.helpers.Stubs._

/**
 * Provides test of performing basic step in/out/over in Scala situations
 * involving assignment.
 *
 * @note Should have a class name of org.senkbeil.test.steps.BasicAssignments
 */
object BasicAssignments {
  def main(args: Array[String]) = {
    val a = "1"
    var b = "2"

    b = a

    noop(None)
  }
}
