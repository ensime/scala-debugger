package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureValueInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockValue = mock[Value]
  private val mockNewPrimitiveProfile = mockFunction[PrimitiveValue, PrimitiveInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
  private val mockNewArrayProfile = mockFunction[ArrayReference, ArrayInfoProfile]
  private val mockNewStringProfile = mockFunction[StringReference, StringInfoProfile]

  describe("PureValueInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockValue

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockValue
        )

        val actual = pureValueInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should supply a type info wrapper even if the value is null") {
        val expected = mock[TypeInfoProfile]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        ) {
          // NOTE: ScalaMock does not allow us to supply null to mock argument,
          //       so throwing an error if we aren't supplied with null
          override protected def newTypeProfile(_type: Type): TypeInfoProfile = {
            require(_type == null)
            expected
          }
        }

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }

      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfoProfile]

        val mockType = mock[Type]
        (mockValue.`type` _).expects().returning(mockType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfoProfile]
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockValue
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfileFunction(_type)
        }

        mockNewTypeProfileFunction.expects(mockType).returning(expected).once()

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#toArrayInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should throw an assertion error if the value is not an array") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should return an array reference wrapped in a profile") {
        val expected = mock[ArrayInfoProfile]
        val mockArrayReference = mock[ArrayReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockArrayReference
        ) {
          override protected def newArrayProfile(
            arrayReference: ArrayReference
          ): ArrayInfoProfile = mockNewArrayProfile(arrayReference)
        }

        mockNewArrayProfile.expects(mockArrayReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toArrayInfo

        actual should be (expected)
      }
    }

    describe("#toStringInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should throw an assertion error if the value is not a string") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should return a string reference wrapped in a profile") {
        val expected = mock[StringInfoProfile]
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockStringReference
        ) {
          override protected def newStringProfile(
            stringReference: StringReference
          ): StringInfoProfile = mockNewStringProfile(stringReference)
        }

        mockNewStringProfile.expects(mockStringReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toStringInfo

        actual should be (expected)
      }
    }

    describe("#toPrimitiveInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should throw an assertion error if the value is not an primitive") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should return a primitive value wrapped in a profile") {
        val expected = mock[PrimitiveInfoProfile]
        val mockPrimitiveValue = mock[PrimitiveValue]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockPrimitiveValue
        ) {
          override protected def newPrimitiveProfile(
            primitiveValue: PrimitiveValue
          ): PrimitiveInfoProfile = mockNewPrimitiveProfile(primitiveValue)
        }

        mockNewPrimitiveProfile.expects(mockPrimitiveValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }

      it("should return a void value wrapped in a profile") {
        val expected = mock[PrimitiveInfoProfile]
        val mockVoidValue = mock[VoidValue]

        val mockNewPrimitiveProfile = mockFunction[VoidValue, PrimitiveInfoProfile]
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockVoidValue
        ) {
          override protected def newPrimitiveProfile(
            voidValue: VoidValue
          ): PrimitiveInfoProfile = mockNewPrimitiveProfile(voidValue)
        }

        mockNewPrimitiveProfile.expects(mockVoidValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }
    }

    describe("#toObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an object") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should return an object reference wrapped in a profile") {
        val expected = mock[ObjectInfoProfile]
        val mockObjectReference = mock[ObjectReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockObjectReference
        ) {
          override protected def newObjectProfile(
            arrayReference: ObjectReference
          ): ObjectInfoProfile = mockNewObjectProfile(arrayReference)
        }

        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toObjectInfo

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should return null if the value is null") {
        val expected: Any = null

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }

      it("should convert the remote value to its underlying value") {
        val expected = "some value"
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockStringReference
        )

        (mockStringReference.value _).expects().returning(expected).once()
        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#isPrimitive") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return false if the value is not a primitive") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is a primitive") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[PrimitiveValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is void (considered primitive)") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[VoidValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }
    }

    describe("#isVoid") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return false if the value is not void") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return true if the value is void") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[VoidValue]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }
    }

    describe("#isObject") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return false if the value is not a object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return true if the value is a object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[ObjectReference]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }
    }

    describe("#isString") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return false if the value is not a string") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return true if the value is a string") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[StringReference]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }
    }

    describe("#isArray") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return false if the value is not a array") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return true if the value is a array") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[ArrayReference]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }
    }

    describe("#isNull") {
      it("should return true if the value is null") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          null
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }

      it("should return false if the value is not null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }
    }
  }
}
