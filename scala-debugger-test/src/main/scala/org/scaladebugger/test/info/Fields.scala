package org.scaladebugger.test.info

/**
 * Tests accessing fields in a variety of different scenarios.
 */
object Fields {
  trait TestTrait {
    val publicBaseFinalPrimitiveField: Int = 999
    val publicBaseFinalObjectField: String = "test"
    var publicBaseMutablePrimitiveField: Int = 1000
    var publicBaseMutableObjectField: String = "test2"
  }

  class InheritedClass(
    override val publicBaseFinalPrimitiveField: Int
  ) extends TestTrait {
    def runMe(): Unit = {
      val local = publicBaseFinalPrimitiveField + publicBaseMutablePrimitiveField
      val local2 = publicBaseFinalObjectField.toString +
        publicBaseMutableObjectField.toString
      println(local)
      println(local2)
    }
  }

  class TestClass(
    private val privateFinalPrimitiveField: Int,
    private val privateFinalObjectField: String,
    private var privateMutablePrimitiveField: Int,
    private var privateMutableObjectField: String,
    protected val protectedFinalPrimitiveField: Int,
    protected val protectedFinalObjectField: String,
    protected var protectedMutablePrimitiveField: Int,
    protected var protectedMutableObjectField: String,
    val publicFinalPrimitiveField: Int,
    val publicFinalObjectField: String,
    var publicMutablePrimitiveField: Int,
    var publicMutableObjectField: String
  ) {
    def runMe(): Unit = {
      val local = privateFinalPrimitiveField + privateMutablePrimitiveField +
        protectedFinalPrimitiveField + protectedMutablePrimitiveField +
        publicFinalPrimitiveField + publicMutablePrimitiveField
      val local2 = privateFinalObjectField + privateMutableObjectField +
        protectedFinalObjectField + protectedMutableObjectField +
        publicFinalObjectField + publicMutableObjectField
      println(local)
      println(local2)
    }
  }

  case class TestCaseClass(primitive: Int, obj: String) {
    def runMe(): Unit = {
      val local = primitive + obj
      val local2 = "another obj"
      println(local)
      println(local2)
    }
  }

  def main(args: Array[String]): Unit = {
    val inheritedClass = new InheritedClass(5)
    inheritedClass.runMe()

    val testClass = new TestClass(0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f")
    testClass.runMe()

    val testCaseClass = TestCaseClass(5, "hello")
    testCaseClass.runMe()

    println("done")
  }
}
