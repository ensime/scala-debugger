package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PurePrimitiveInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVoidValue = mock[VoidValue]
  private val mockPrimitiveValue = mock[PrimitiveValue]

  describe("PurePrimitiveInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation when wrapping primitive value") {
        val expected = mock[PrimitiveInfoProfile]
        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveInfoProfile(_: ScalaVirtualMachine, _: PrimitiveValue))
          .expects(mockScalaVirtualMachine, mockPrimitiveValue)
          .returning(expected).once()

        val actual = purePrimitiveInfoProfile.toJavaInfo

        actual should be (expected)
      }

      it("should return a new instance of the Java profile representation when wrapping void value") {
        val expected = mock[PrimitiveInfoProfile]
        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveInfoProfile(_: ScalaVirtualMachine, _: VoidValue))
          .expects(mockScalaVirtualMachine, mockVoidValue)
          .returning(expected).once()

        val actual = purePrimitiveInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        val actual = purePrimitiveInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance of void if representing a void") {
        val expected = mockVoidValue

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.toJdiInstance

        actual should be (expected)
      }

      it("should return the JDI instance of primitive if representing a primitive") {
        val expected = mockPrimitiveValue

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        )

        val actual = purePrimitiveInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should throw an error if representing a void value") {
        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        intercept[AssertionError] {
          purePrimitiveInfoProfile.toLocalValue
        }
      }

      it("should return a primitive if representing a primitive instance") {
        val expected = 2.toByte

        val mockByteValue = mock[ByteValue]
        (mockByteValue.value _).expects().returning(expected).once()

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockByteValue)
        )

        val actual = purePrimitiveInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new primitive type profile wrapping the primitive type") {
        val expected = mock[PrimitiveTypeInfoProfile]

        val mockPrimitiveType = mock[PrimitiveType]
        (mockPrimitiveValue.`type` _).expects()
          .returning(mockPrimitiveType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfoProfile]
        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockPrimitiveValue)
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfileFunction(_type)
        }

        val mockTypeInfoProfile = mock[TypeInfoProfile]
        mockNewTypeProfileFunction.expects(mockPrimitiveType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toPrimitiveType _).expects()
          .returning(expected).once()

        val actual = purePrimitiveInfoProfile.typeInfo

        actual should be (expected)
      }

      it("should should return a new primitive type profile wrapping the void type") {
        val expected = mock[PrimitiveTypeInfoProfile]

        val mockVoidType = mock[VoidType]
        (mockVoidValue.`type` _).expects()
          .returning(mockVoidType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfoProfile]
        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfileFunction(_type)
        }

        val mockTypeInfoProfile = mock[TypeInfoProfile]
        mockNewTypeProfileFunction.expects(mockVoidType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toPrimitiveType _).expects()
          .returning(expected).once()

        val actual = purePrimitiveInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#isBoolean") {
      it("should return true if the primitive value is a boolean") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[BooleanValue])
        )

        val actual = purePrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }

      it("should return false if the primitive value is not a boolean") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }
    }

    describe("#isByte") {
      it("should return true if the primitive value is a byte") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[ByteValue])
        )

        val actual = purePrimitiveInfoProfile.isByte

        actual should be (expected)
      }

      it("should return false if the primitive value is not a byte") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isByte

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isByte

        actual should be (expected)
      }
    }

    describe("#isChar") {
      it("should return true if the primitive value is a char") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[CharValue])
        )

        val actual = purePrimitiveInfoProfile.isChar

        actual should be (expected)
      }

      it("should return false if the primitive value is not a char") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isChar

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isChar

        actual should be (expected)
      }
    }

    describe("#isInteger") {
      it("should return true if the primitive value is an integer") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[IntegerValue])
        )

        val actual = purePrimitiveInfoProfile.isInteger

        actual should be (expected)
      }

      it("should return false if the primitive value is not an integer") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isInteger

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isInteger

        actual should be (expected)
      }
    }

    describe("#isLong") {
      it("should return true if the primitive value is a long") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[LongValue])
        )

        val actual = purePrimitiveInfoProfile.isLong

        actual should be (expected)
      }

      it("should return false if the primitive value is not a long") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isLong

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isLong

        actual should be (expected)
      }
    }

    describe("#isShort") {
      it("should return true if the primitive value is a short") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[ShortValue])
        )

        val actual = purePrimitiveInfoProfile.isShort

        actual should be (expected)
      }

      it("should return false if the primitive value is not a short") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isShort

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isShort

        actual should be (expected)
      }
    }

    describe("#isDouble") {
      it("should return true if the primitive value is a double") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[DoubleValue])
        )

        val actual = purePrimitiveInfoProfile.isDouble

        actual should be (expected)
      }

      it("should return false if the primitive value is not a double") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isDouble

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isDouble

        actual should be (expected)
      }
    }

    describe("#isFloat") {
      it("should return true if the primitive value is a float") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[FloatValue])
        )

        val actual = purePrimitiveInfoProfile.isFloat

        actual should be (expected)
      }

      it("should return false if the primitive value is not a float") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[PrimitiveValue])
        )

        val actual = purePrimitiveInfoProfile.isFloat

        actual should be (expected)
      }

      it("should return false if representing a void value") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockVoidValue)
        )

        val actual = purePrimitiveInfoProfile.isFloat

        actual should be (expected)
      }
    }
  }
}
