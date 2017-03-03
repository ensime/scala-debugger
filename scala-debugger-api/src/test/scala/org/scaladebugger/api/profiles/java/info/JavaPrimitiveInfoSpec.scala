package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaPrimitiveInfoSpec extends ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVoidValue = mock[VoidValue]
  private val mockPrimitiveValue = mock[PrimitiveValue]

  describe("JavaPrimitiveInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation when wrapping primitive value") {
        val expected = mock[PrimitiveInfo]
        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveInfo(_: ScalaVirtualMachine, _: PrimitiveValue))
          .expects(mockScalaVirtualMachine, mockPrimitiveValue)
          .returning(expected).once()

        val actual = javaPrimitiveInfoProfile.toJavaInfo

        actual should be (expected)
      }

      it("should return a new instance of the Java profile representation when wrapping void value") {
        val expected = mock[PrimitiveInfo]
        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveInfo(_: ScalaVirtualMachine, _: VoidValue))
          .expects(mockScalaVirtualMachine, mockVoidValue)
          .returning(expected).once()

        val actual = javaPrimitiveInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        val actual = javaPrimitiveInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance of void if representing a void") {
        val expected = mockVoidValue

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.toJdiInstance

        actual should be (expected)
      }

      it("should return the JDI instance of primitive if representing a primitive") {
        val expected = mockPrimitiveValue

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        val actual = javaPrimitiveInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should throw an error if representing a void value") {
        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        intercept[AssertionError] {
          javaPrimitiveInfoProfile.toLocalValue
        }
      }

      it("should return a primitive if representing a primitive instance") {
        val expected = 2.toByte

        val mockByteValue = mock[ByteValue]
        (mockByteValue.value _).expects().returning(expected).once()

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockByteValue)
        )

        val actual = javaPrimitiveInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new primitive type profile wrapping the primitive type") {
        val expected = mock[PrimitiveTypeInfo]

        val mockPrimitiveType = mock[PrimitiveType]
        (mockPrimitiveValue.`type` _).expects()
          .returning(mockPrimitiveType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfo]
        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfileFunction(_type)
        }

        val mockTypeInfoProfile = mock[TypeInfo]
        mockNewTypeProfileFunction.expects(mockPrimitiveType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toPrimitiveType _).expects()
          .returning(expected).once()

        val actual = javaPrimitiveInfoProfile.`type`

        actual should be (expected)
      }

      it("should should return a new primitive type profile wrapping the void type") {
        val expected = mock[PrimitiveTypeInfo]

        val mockVoidType = mock[VoidType]
        (mockVoidValue.`type` _).expects()
          .returning(mockVoidType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfo]
        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfileFunction(_type)
        }

        val mockTypeInfoProfile = mock[TypeInfo]
        mockNewTypeProfileFunction.expects(mockVoidType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toPrimitiveType _).expects()
          .returning(expected).once()

        val actual = javaPrimitiveInfoProfile.`type`

        actual should be (expected)
      }
    }

    describe("#isBoolean") {
      it("should return true if the primitive value is a boolean") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[BooleanValue])
        )

        val actual = javaPrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }

      it("should return false if the primitive value is not a boolean") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }
    }

    describe("#isByte") {
      it("should return true if the primitive value is a byte") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[ByteValue])
        )

        val actual = javaPrimitiveInfoProfile.isByte

        actual should be (expected)
      }

      it("should return false if the primitive value is not a byte") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isByte

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isByte

        actual should be (expected)
      }
    }

    describe("#isChar") {
      it("should return true if the primitive value is a char") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[CharValue])
        )

        val actual = javaPrimitiveInfoProfile.isChar

        actual should be (expected)
      }

      it("should return false if the primitive value is not a char") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isChar

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isChar

        actual should be (expected)
      }
    }

    describe("#isInteger") {
      it("should return true if the primitive value is an integer") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[IntegerValue])
        )

        val actual = javaPrimitiveInfoProfile.isInteger

        actual should be (expected)
      }

      it("should return false if the primitive value is not an integer") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isInteger

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isInteger

        actual should be (expected)
      }
    }

    describe("#isLong") {
      it("should return true if the primitive value is a long") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[LongValue])
        )

        val actual = javaPrimitiveInfoProfile.isLong

        actual should be (expected)
      }

      it("should return false if the primitive value is not a long") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isLong

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isLong

        actual should be (expected)
      }
    }

    describe("#isShort") {
      it("should return true if the primitive value is a short") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[ShortValue])
        )

        val actual = javaPrimitiveInfoProfile.isShort

        actual should be (expected)
      }

      it("should return false if the primitive value is not a short") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isShort

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isShort

        actual should be (expected)
      }
    }

    describe("#isDouble") {
      it("should return true if the primitive value is a double") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[DoubleValue])
        )

        val actual = javaPrimitiveInfoProfile.isDouble

        actual should be (expected)
      }

      it("should return false if the primitive value is not a double") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isDouble

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isDouble

        actual should be (expected)
      }
    }

    describe("#isFloat") {
      it("should return true if the primitive value is a float") {
        val expected = true

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[FloatValue])
        )

        val actual = javaPrimitiveInfoProfile.isFloat

        actual should be (expected)
      }

      it("should return false if the primitive value is not a float") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = javaPrimitiveInfoProfile.isFloat

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val javaPrimitiveInfoProfile = new JavaPrimitiveInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = javaPrimitiveInfoProfile.isFloat

        actual should be (expected)
      }
    }
  }
}
