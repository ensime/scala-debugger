package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaTypeInfoSpec extends ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockType = mock[Type]
  private val javaTypeInfoProfile = new JavaTypeInfo(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockType
  )

  describe("JavaTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[TypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newTypeInfo _)
          .expects(mockScalaVirtualMachine, mockType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockType

        val actual = javaTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the type if not null") {
        val expected = "some.type.name"

        (mockType.name _).expects().returning(expected).once()

        val actual = javaTypeInfoProfile.name

        actual should be (expected)
      }

      it("should return \"null\" if the type is null") {
        val expected = "null"

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#signature") {
      it("should return the name of the type if not null") {
        val expected = "signature"

        (mockType.signature _).expects().returning(expected).once()

        val actual = javaTypeInfoProfile.signature

        actual should be (expected)
      }

      it("should return \"null\" if the type is null") {
        val expected = "null"

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.signature

        actual should be (expected)
      }
    }

    describe("#isArrayType") {
      it("should return true if the underlying type is an array type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ArrayType]
        )
        val actual = javaTypeInfoProfile.isArrayType

        actual should be (expected)
      }

      it("should return false if the underlying type is not an array type") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isArrayType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isArrayType

        actual should be (expected)
      }
    }

    describe("#isClassType") {
      it("should return true if the underlying type is a class type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ClassType]
        )
        val actual = javaTypeInfoProfile.isClassType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a class type") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isClassType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isClassType

        actual should be (expected)
      }
    }

    describe("#isInterfaceType") {
      it("should return true if the underlying type is an interface type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[InterfaceType]
        )
        val actual = javaTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }

      it("should return false if the underlying type is not an interface type") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isInterfaceType

        actual should be (expected)
      }
    }

    describe("#isReferenceType") {
      it("should return true if the underlying type is a reference type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[ReferenceType]
        )
        val actual = javaTypeInfoProfile.isReferenceType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a reference type") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isReferenceType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isReferenceType

        actual should be (expected)
      }
    }

    describe("#isPrimitiveType") {
      it("should return true if the underlying type is a primitive type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[PrimitiveType]
        )
        val actual = javaTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return true if the underlying type is a void type") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[VoidType]
        )
        val actual = javaTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return false if the underlying type is not a primitive or void type") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }

      it("should return false if the underlying type is null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isPrimitiveType

        actual should be (expected)
      }
    }

    describe("#isNullType") {
      it("should return false if the underlying type is not null") {
        val expected = false

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )
        val actual = javaTypeInfoProfile.isNullType

        actual should be (expected)
      }

      it("should return true if the underlying type is null") {
        val expected = true

        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, null
        )
        val actual = javaTypeInfoProfile.isNullType

        actual should be (expected)
      }
    }

    describe("#toArrayType") {
      it("should throw an error if the type is not an array") {
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          javaTypeInfoProfile.toArrayType
        }
      }

      it("should return a wrapper around the array type") {
        val expected = mock[ArrayTypeInfo]

        val mockArrayType = mock[ArrayType]
        val mockNewArrayTypeProfile = mockFunction[ArrayType, ArrayTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayType
        ) {
          override protected def newArrayTypeProfile(
            arrayType: ArrayType
          ): ArrayTypeInfo = mockNewArrayTypeProfile(arrayType)
        }

        mockNewArrayTypeProfile.expects(mockArrayType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toArrayType

        actual should be (expected)
      }
    }

    describe("#toClassType") {
      it("should throw an error if the type is not a class") {
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          javaTypeInfoProfile.toClassType
        }
      }

      it("should return a wrapper around the class type") {
        val expected = mock[ClassTypeInfo]

        val mockClassType = mock[ClassType]
        val mockNewClassTypeProfile = mockFunction[ClassType, ClassTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockClassType
        ) {
          override protected def newClassTypeProfile(
            classType: ClassType
          ): ClassTypeInfo = mockNewClassTypeProfile(classType)
        }

        mockNewClassTypeProfile.expects(mockClassType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toClassType

        actual should be (expected)
      }
    }

    describe("#toInterfaceType") {
      it("should throw an error if the type is not an interface") {
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          javaTypeInfoProfile.toInterfaceType
        }
      }

      it("should return a wrapper around the interface type") {
        val expected = mock[InterfaceTypeInfo]

        val mockInterfaceType = mock[InterfaceType]
        val mockNewInterfaceTypeProfile = mockFunction[InterfaceType, InterfaceTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockInterfaceType
        ) {
          override protected def newInterfaceTypeProfile(
            interfaceType: InterfaceType
          ): InterfaceTypeInfo = mockNewInterfaceTypeProfile(interfaceType)
        }

        mockNewInterfaceTypeProfile.expects(mockInterfaceType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toInterfaceType

        actual should be (expected)
      }
    }

    describe("#toReferenceType") {
      it("should throw an error if the type is not a reference") {
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          javaTypeInfoProfile.toReferenceType
        }
      }

      it("should return a wrapper around the reference type") {
        val expected = mock[ReferenceTypeInfo]

        val mockReferenceType = mock[ReferenceType]
        val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockReferenceType
        ) {
          override protected def newReferenceTypeProfile(
            referenceType: ReferenceType
          ): ReferenceTypeInfo = mockNewReferenceTypeProfile(referenceType)
        }

        mockNewReferenceTypeProfile.expects(mockReferenceType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toReferenceType

        actual should be (expected)
      }
    }

    describe("#toPrimitiveType") {
      it("should throw an error if the type is not a primitive") {
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mock[Type]
        )

        intercept[AssertionError] {
          javaTypeInfoProfile.toPrimitiveType
        }
      }

      it("should return a wrapper around the primitive type") {
        val expected = mock[PrimitiveTypeInfo]

        val mockPrimitiveType = mock[PrimitiveType]
        val mockNewPrimitiveTypeProfile = mockFunction[PrimitiveType, PrimitiveTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockPrimitiveType
        ) {
          override protected def newPrimitiveTypeProfile(
            primitiveType: PrimitiveType
          ): PrimitiveTypeInfo = mockNewPrimitiveTypeProfile(primitiveType)
        }

        mockNewPrimitiveTypeProfile.expects(mockPrimitiveType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toPrimitiveType

        actual should be (expected)
      }

      it("should return a wrapper around the void type") {
        val expected = mock[PrimitiveTypeInfo]

        val mockVoidType = mock[VoidType]
        val mockNewPrimitiveTypeProfile = mockFunction[VoidType, PrimitiveTypeInfo]
        val javaTypeInfoProfile = new JavaTypeInfo(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockVoidType
        ) {
          override protected def newPrimitiveTypeProfile(
            voidType: VoidType
          ): PrimitiveTypeInfo = mockNewPrimitiveTypeProfile(voidType)
        }

        mockNewPrimitiveTypeProfile.expects(mockVoidType)
          .returning(expected).once()

        val actual = javaTypeInfoProfile.toPrimitiveType

        actual should be (expected)
      }
    }
  }
}
