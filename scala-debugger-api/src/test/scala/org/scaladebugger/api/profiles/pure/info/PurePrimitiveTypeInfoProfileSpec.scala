package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PurePrimitiveTypeInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockPrimitiveType = mock[PrimitiveType]
  private val mockVoidType = mock[VoidType]

  private val leftPrimitiveTypeInfoProfile = new PurePrimitiveTypeInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, Left(mockPrimitiveType)
  )

  private val rightPrimitiveTypeInfoProfile = new PurePrimitiveTypeInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, Right(mockVoidType)
  )

  describe("PurePrimitiveTypeInfoProfile") {
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
