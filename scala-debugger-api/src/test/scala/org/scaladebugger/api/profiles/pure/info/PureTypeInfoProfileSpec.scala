package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureTypeInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockType = mock[Type]
  private val pureTypeInfoProfile = new PureTypeInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockType
  )

  describe("PureTypeInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockType

        val actual = pureTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the type if not null") {
        val expected = "some.type.name"

        (mockType.name _).expects().returning(expected).once()

        val actual = pureTypeInfoProfile.name

        actual should be (expected)
      }

      it("should return \"null\" if the type is null") {
        val expected = "null"

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#signature") {
      it("should return the name of the type if not null") {
        val expected = "signature"

        (mockType.signature _).expects().returning(expected).once()

        val actual = pureTypeInfoProfile.signature

        actual should be (expected)
      }

      it("should return \"null\" if the type is null") {
        val expected = "null"

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.signature

        actual should be (expected)
      }
    }

    describe("#isArrayType") {
      it("should return true if the underlying type is an array type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ArrayType]
        )
        val actual = pureTypeInfoProfile.isArrayType

        actual should be (expected)
      }

      it("should return false if the underlying type is not an array type") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isArrayType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isArrayType

        actual should be (expected)
      }
    }

    describe("#isClassType") {
      it("should return true if the underlying type is a class type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ClassType]
        )
        val actual = pureTypeInfoProfile.isClassType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a class type") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isClassType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isClassType

        actual should be (expected)
      }
    }

    describe("#isInterfaceType") {
      it("should return true if the underlying type is an interface type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[InterfaceType]
        )
        val actual = pureTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }

      it("should return false if the underlying type is not an interface type") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }
    }

    describe("#isReferenceType") {
      it("should return true if the underlying type is a reference type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ReferenceType]
        )
        val actual = pureTypeInfoProfile.isReferenceType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a reference type") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isReferenceType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isReferenceType

        actual should be (expected)
      }
    }

    describe("#isPrimitiveType") {
      it("should return true if the underlying type is a primitive type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[PrimitiveType]
        )
        val actual = pureTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return true if the underlying type is a void type") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[VoidType]
        )
        val actual = pureTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a primitive or void type") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }
    }

    describe("#isNullType") {
      it("should return false if the underlying type is not null") {
        val expected = false

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = pureTypeInfoProfile.isNullType

        actual should be (expected)
      }

      it("should return true if the underlying type is null") {
        val expected = true

        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = pureTypeInfoProfile.isNullType

        actual should be (expected)
      }
    }

    describe("#toArrayType") {
      it("should throw an error if the type is not an array") {
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          pureTypeInfoProfile.toArrayType
        }
      }

      it("should return a wrapper around the array type") {
        val expected = mock[ArrayTypeInfoProfile]

        val mockArrayType = mock[ArrayType]
        val mockNewArrayTypeProfile = mockFunction[ArrayType, ArrayTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayType
        ) {
          override protected def newArrayTypeProfile(
            arrayType: ArrayType
          ): ArrayTypeInfoProfile = mockNewArrayTypeProfile(arrayType)
        }

        mockNewArrayTypeProfile.expects(mockArrayType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toArrayType

        actual should be (expected)
      }
    }

    describe("#toClassType") {
      it("should throw an error if the type is not a class") {
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          pureTypeInfoProfile.toClassType
        }
      }

      it("should return a wrapper around the class type") {
        val expected = mock[ClassTypeInfoProfile]

        val mockClassType = mock[ClassType]
        val mockNewClassTypeProfile = mockFunction[ClassType, ClassTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockClassType
        ) {
          override protected def newClassTypeProfile(
            classType: ClassType
          ): ClassTypeInfoProfile = mockNewClassTypeProfile(classType)
        }

        mockNewClassTypeProfile.expects(mockClassType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toClassType

        actual should be (expected)
      }
    }

    describe("#toInterfaceType") {
      it("should throw an error if the type is not an interface") {
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          pureTypeInfoProfile.toInterfaceType
        }
      }

      it("should return a wrapper around the interface type") {
        val expected = mock[InterfaceTypeInfoProfile]

        val mockInterfaceType = mock[InterfaceType]
        val mockNewInterfaceTypeProfile = mockFunction[InterfaceType, InterfaceTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockInterfaceType
        ) {
          override protected def newInterfaceTypeProfile(
            interfaceType: InterfaceType
          ): InterfaceTypeInfoProfile = mockNewInterfaceTypeProfile(interfaceType)
        }

        mockNewInterfaceTypeProfile.expects(mockInterfaceType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toInterfaceType

        actual should be (expected)
      }
    }

    describe("#toReferenceType") {
      it("should throw an error if the type is not a reference") {
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          pureTypeInfoProfile.toReferenceType
        }
      }

      it("should return a wrapper around the reference type") {
        val expected = mock[ReferenceTypeInfoProfile]

        val mockReferenceType = mock[ReferenceType]
        val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockReferenceType
        ) {
          override protected def newReferenceTypeProfile(
            referenceType: ReferenceType
          ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)
        }

        mockNewReferenceTypeProfile.expects(mockReferenceType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toReferenceType

        actual should be (expected)
      }
    }

    describe("#toPrimitiveType") {
      it("should throw an error if the type is not a primitive") {
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          pureTypeInfoProfile.toPrimitiveType
        }
      }

      it("should return a wrapper around the primitive type") {
        val expected = mock[PrimitiveTypeInfoProfile]

        val mockPrimitiveType = mock[PrimitiveType]
        val mockNewPrimitiveTypeProfile = mockFunction[PrimitiveType, PrimitiveTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockPrimitiveType
        ) {
          override protected def newPrimitiveTypeProfile(
            primitiveType: PrimitiveType
          ): PrimitiveTypeInfoProfile = mockNewPrimitiveTypeProfile(primitiveType)
        }

        mockNewPrimitiveTypeProfile.expects(mockPrimitiveType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toPrimitiveType

        actual should be (expected)
      }

      it("should return a wrapper around the void type") {
        val expected = mock[PrimitiveTypeInfoProfile]

        val mockVoidType = mock[VoidType]
        val mockNewPrimitiveTypeProfile = mockFunction[VoidType, PrimitiveTypeInfoProfile]
        val pureTypeInfoProfile = new PureTypeInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockVoidType
        ) {
          override protected def newPrimitiveTypeProfile(
            voidType: VoidType
          ): PrimitiveTypeInfoProfile = mockNewPrimitiveTypeProfile(voidType)
        }

        mockNewPrimitiveTypeProfile.expects(mockVoidType)
          .returning(expected).once()

        val actual = pureTypeInfoProfile.toPrimitiveType

        actual should be (expected)
      }
    }
  }
}
