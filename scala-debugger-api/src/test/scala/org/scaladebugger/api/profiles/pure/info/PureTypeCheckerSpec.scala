package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{MethodInfo, ValueInfo, VariableInfo}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureTypeCheckerSpec extends test.ParallelMockFunSpec
{
  private val pureTypeCheckerProfile = new PureTypeChecker

  describe("PureTypeChecker") {
    describe("#equalTypeNames") {
      it("should return true for string equal type names") {
        val expected = true

        val t1 = "some.type"
        val t2 = "some.type"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Boolean and boolean") {
        val expected = true

        val t1 = "java.lang.Boolean"
        val t2 = "boolean"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Byte and byte") {
        val expected = true

        val t1 = "java.lang.Byte"
        val t2 = "byte"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Char and char") {
        val expected = true

        val t1 = "java.lang.Char"
        val t2 = "char"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Integer and int") {
        val expected = true

        val t1 = "java.lang.Integer"
        val t2 = "int"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Short and short") {
        val expected = true

        val t1 = "java.lang.Short"
        val t2 = "short"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Long and long") {
        val expected = true

        val t1 = "java.lang.Long"
        val t2 = "long"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Float and float") {
        val expected = true

        val t1 = "java.lang.Float"
        val t2 = "float"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return true for java.lang.Double and double") {
        val expected = true

        val t1 = "java.lang.Double"
        val t2 = "double"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }

      it("should return false when not string equal or primitive equivalent") {
        val expected = false

        val t1 = "some.type"
        val t2 = "some.other.type"

        val actual = pureTypeCheckerProfile.equalTypeNames(t1, t2)

        actual should be (expected)
      }
    }
  }
}
