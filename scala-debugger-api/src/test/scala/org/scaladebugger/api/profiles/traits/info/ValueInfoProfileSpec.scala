package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestValueInfoProfile

import scala.util.Failure

class ValueInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ValueInfoProfile") {
    describe("#toPrettyString") {
      it("should display null if the value is null") {
        val expected = "null"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = true
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display void if the value is void") {
        val expected = "void"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = true
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the string instnace if a string") {
        val expected = "STRING"

        val mockStringInfoProfile = mock[StringInfoProfile]
        (mockStringInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toStringInfo: StringInfoProfile = mockStringInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the array instance if an array") {
        val expected = "ARRAY"

        val mockArrayInfoProfile = mock[ArrayInfoProfile]
        (mockArrayInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = true
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toArrayInfo: ArrayInfoProfile = mockArrayInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the object instance if an object") {
        val expected = "OBJECT"

        val mockObjectInfoProfile = mock[ObjectInfoProfile]
        (mockObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toObjectInfo: ObjectInfoProfile = mockObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the primitive instance if a primitive") {
        val expected = "ARRAY"

        val mockPrimitiveInfoProfile = mock[PrimitiveInfoProfile]
        (mockPrimitiveInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = true
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toPrimitiveInfo: PrimitiveInfoProfile = mockPrimitiveInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? if unrecognized value") {
        val expected = "???"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display <ERROR> if failed to retrieve value") {
        val expected = "<ERROR>"

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = throw new Throwable
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryToPrimitiveInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[PrimitiveInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toPrimitiveInfo: PrimitiveInfoProfile = mockUnsafeMethod()
        }

        val r = mock[PrimitiveInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToPrimitiveInfo.get should be (r)
      }
    }

    describe("#tryToObjectInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toObjectInfo: ObjectInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToObjectInfo.get should be (r)
      }
    }

    describe("#tryToLocalValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Any]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = mockUnsafeMethod()
        }

        val r = 3
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToLocalValue.get should be (r)
      }
    }

    describe("#tryToStringInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[StringInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toStringInfo: StringInfoProfile = mockUnsafeMethod()
        }

        val r = mock[StringInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToStringInfo.get should be (r)
      }
    }

    describe("#tryToArrayInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ArrayInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toArrayInfo: ArrayInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ArrayInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToArrayInfo.get should be (r)
      }
    }
  }
}
