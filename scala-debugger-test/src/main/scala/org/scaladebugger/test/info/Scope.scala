package org.scaladebugger.test.info

import org.scaladebugger.test.helpers.Stubs._

/**
 * Provides test of various scope-related naming issues.
 *
 * @note Should have a class name of org.scaladebugger.test.info.Scope
 */
object Scope {
  var field1 = 1.0

  // Introduce MODULE$
  while (field1 < 5.0) {
    val newValue = field1 + 1.0
    field1 = newValue
  }

  // Introduce hidden field1
  class OuterScopeClass(a: Int, aa: String) {
    def runMe() = {
      field1 + a + aa
    }
  }

  def main(args: Array[String]) = {
    val x = 33
    val y = "test"

    // Introduce x$1 and y$1 in function closure
    val closureFunc = () => {
      println(x + y)
      noop(None)
    }

    // Introduce x$1 and y$1 in class closure
    class ClosureClass(z: Int, zz: String) {
      def runMe() = {
        x + y + z + zz
      }
    }

    closureFunc()
    println(new ClosureClass(999, "other").runMe())
    println(new OuterScopeClass(999, "other").runMe())
    noop(None)
  }
}
