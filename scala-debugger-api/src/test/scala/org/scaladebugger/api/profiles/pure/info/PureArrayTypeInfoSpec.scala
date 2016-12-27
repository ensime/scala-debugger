package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfo, InfoProducer, TypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.api.profiles.traits.info.ArrayTypeInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class PureArrayTypeInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockNewArrayProfile = mockFunction[ArrayReference, ArrayInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockArrayType = mock[ArrayType]
  private val pureArrayTypeInfoProfile = new PureArrayTypeInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockArrayType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)

    override protected def newArrayProfile(
      arrayReference: ArrayReference
    ): ArrayInfo = mockNewArrayProfile(arrayReference)
  }

  describe("PureArrayTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ArrayTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newArrayTypeInfoProfile _)
          .expects(mockScalaVirtualMachine, mockArrayType)
          .returning(expected).once()

        val actual = pureArrayTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureArrayTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockArrayType

        val actual = pureArrayTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#elementSignature") {
      it("should return the signature of the array's element") {
        val expected = "signature"

        (mockArrayType.componentSignature _).expects()
          .returning(expected).once()

        val actual = pureArrayTypeInfoProfile.elementSignature

        actual should be (expected)
      }
    }

    describe("#elementTypeName") {
      it("should return the type name of the array's element") {
        val expected = "some.type.name"

        (mockArrayType.componentTypeName _).expects()
          .returning(expected).once()

        val actual = pureArrayTypeInfoProfile.elementTypeName

        actual should be (expected)
      }
    }

    describe("#elementTypeInfo") {
      it("should should return a new type info profile wrapping the array element's type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockArrayType.componentType _).expects()
          .returning(mockType).once()

        mockNewTypeProfile.expects(mockType).returning(expected).once()

        val actual = pureArrayTypeInfoProfile.elementTypeInfo

        actual should be (expected)
      }
    }

    describe("#newInstance") {
      it("should create a new instance of the array with the specified length") {
        val expected = mock[ArrayInfo]
        val length = 999

        val mockArrayReference = mock[ArrayReference]
        (mockArrayType.newInstance _).expects(length)
          .returning(mockArrayReference).once()

        mockNewArrayProfile.expects(mockArrayReference)
          .returning(expected).once()

        val actual = pureArrayTypeInfoProfile.newInstance(length)

        actual should be (expected)
      }
    }
  }
}
