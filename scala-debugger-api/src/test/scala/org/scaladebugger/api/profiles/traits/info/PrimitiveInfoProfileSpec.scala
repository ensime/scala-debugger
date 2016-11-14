package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestPrimitiveInfoProfile

class PrimitiveInfoProfileSpec extends test.ParallelMockFunSpec
{
  describe("PrimitiveInfoProfile") {
    describe("#tryToLocalValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[AnyVal]

        val primitiveInfoProfile = new TestPrimitiveInfoProfile {
          override def toLocalValue: AnyVal = mockUnsafeMethod()
        }

        val r = 3
        mockUnsafeMethod.expects().returning(r).once()
        primitiveInfoProfile.tryToLocalValue.get should be (r)
      }
    }

    describe("#toPrettyString") {
      it("should display the value as a string") {
        val expected = "333"

        val primitiveInfoProfile = new TestPrimitiveInfoProfile {
          override def isChar: Boolean = false
          override def toLocalValue: AnyVal = 333
        }

        val actual = primitiveInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should wrap a character value in single quotes") {
        val expected = "'a'"

        val primitiveInfoProfile = new TestPrimitiveInfoProfile {
          override def isChar: Boolean = true
          override def toLocalValue: AnyVal = 'a'
        }

        val actual = primitiveInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should return <ERROR> if unable to get the value") {
        val expected = "<ERROR>"

        val primitiveInfoProfile = new TestPrimitiveInfoProfile {
          override def isChar: Boolean = false
          override def toLocalValue: AnyVal = throw new Throwable
        }

        val actual = primitiveInfoProfile.toPrettyString

        actual should be(expected)
      }
    }
  }
}
