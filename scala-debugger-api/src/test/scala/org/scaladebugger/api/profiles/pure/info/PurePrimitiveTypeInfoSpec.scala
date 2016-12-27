package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.api.profiles.traits.info.PrimitiveTypeInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class PurePrimitiveTypeInfoSpec extends ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockPrimitiveType = mock[PrimitiveType]
  private val mockVoidType = mock[VoidType]

  private val leftPrimitiveTypeInfoProfile = new PurePrimitiveTypeInfo(
    mockScalaVirtualMachine, mockInfoProducerProfile, Left(mockPrimitiveType)
  )

  private val rightPrimitiveTypeInfoProfile = new PurePrimitiveTypeInfo(
    mockScalaVirtualMachine, mockInfoProducerProfile, Right(mockVoidType)
  )

  describe("PurePrimitiveTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation when wrapping primitive type") {
        val expected = mock[PrimitiveTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveTypeInfoProfile(_: ScalaVirtualMachine, _: PrimitiveType))
          .expects(mockScalaVirtualMachine, mockPrimitiveType)
          .returning(expected).once()

        val actual = leftPrimitiveTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }

      it("should return a new instance of the Java profile representation when wrapping void type") {
        val expected = mock[PrimitiveTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newPrimitiveTypeInfoProfile(_: ScalaVirtualMachine, _: VoidType))
          .expects(mockScalaVirtualMachine, mockVoidType)
          .returning(expected).once()

        val actual = rightPrimitiveTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = leftPrimitiveTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        leftPrimitiveTypeInfoProfile.toJdiInstance should be (mockPrimitiveType)
        rightPrimitiveTypeInfoProfile.toJdiInstance should be (mockVoidType)
      }
    }

    describe("#name") {
      it("should return the name of the type") {
        val expected = "some.type.name"

        (mockPrimitiveType.name _).expects().returning(expected).once()
        (mockVoidType.name _).expects().returning(expected).once()

        val actual1 = leftPrimitiveTypeInfoProfile.name
        val actual2 = rightPrimitiveTypeInfoProfile.name

        actual1 should be (expected)
        actual2 should be (expected)
      }
    }

    describe("#signature") {
      it("should return the signature of the type") {
        val expected = "signature"

        (mockPrimitiveType.signature _).expects().returning(expected).once()
        (mockVoidType.signature _).expects().returning(expected).once()

        val actual1 = leftPrimitiveTypeInfoProfile.signature
        val actual2 = rightPrimitiveTypeInfoProfile.signature

        actual1 should be (expected)
        actual2 should be (expected)
      }
    }

    describe("#toPrettyString") {
      it("should return a string with the type name and signature") {
        val name = "some.type.name"
        val signature = "signature"
        val expected = s"Type $name ($signature)"

        (mockPrimitiveType.name _).expects().returning(name).once()
        (mockVoidType.name _).expects().returning(name).once()
        (mockPrimitiveType.signature _).expects().returning(signature).once()
        (mockVoidType.signature _).expects().returning(signature).once()

        val actual1 = leftPrimitiveTypeInfoProfile.toPrettyString
        val actual2 = rightPrimitiveTypeInfoProfile.toPrettyString

        actual1 should be (expected)
        actual2 should be (expected)
      }
    }
  }
}
