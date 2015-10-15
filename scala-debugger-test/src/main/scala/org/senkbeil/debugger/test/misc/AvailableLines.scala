package org.senkbeil.debugger.test.misc

/**
 * Provides test of various situations where lines might be breakpointable.
 *
 * @note Should have a class name of org.senkbeil.test.AvailableLines
 * @note Should have breakpoint lines on
 *       11, 12, 13, 14, 15, 16, 20, 21, 22, 26, 27, 28, 32, 34, 35, 37, 39,
 *       40, 41, 42, 45, 46, 47, 50, 52, 53, 57, 58, 59, 60, 63, 65
 */
object AvailableLines {
  private val objectFunc = () => {
    println("This")
    println("Is")
    val otherText = "Attached to an object"
    println(otherText)
  }

  private def objectMethod(a: Int, b: Long, c: String) = {
    a +
    b +
    c
  }

  private def unusedMethod() = {
    val x = 1
    val y = 2
    x + y
  }

  def main(args: Array[String]) = {
    val immutableInt = 3

    var mutableInt = 2
    mutableInt = immutableInt + mutableInt

    val singleLineFunction = (x: Int, y: Int) => x + y

    val multiLineFunction = (x: Int, y: Int) => {
      val a = x + 2
      val b = y - 2
      a + b
    }

    val singleLineResult = singleLineFunction(
      3,
      2
    )

    val multiLineResult = multiLineFunction(
      {
        3 + 2
      }, 5
    )

    def inlineMethod() = {
      println(singleLineFunction(1, 2))
      val result = "test"
      println(multiLineFunction(100, 200))
      result
    }

    inlineMethod()

    while (true) { Thread.sleep(1) }

    // These lines should not be breakpointable (not reachable)
    val unreachableImmutable = 1
    var unreachableMutable = 1
    unreachableMutable += unreachableImmutable
  }
}
