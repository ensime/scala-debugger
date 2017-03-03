package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaClassObjectInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockClassObjectReference = mock[ClassObjectReference]
  private val javaClassObjectInfoProfile = new JavaClassObjectInfo(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockClassObjectReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("JavaClassObjectInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ClassObjectInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newClassObjectInfo(
          _: ScalaVirtualMachine,
          _: ClassObjectReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockClassObjectReference,
          *, *
        ).returning(expected).once()

        val actual = javaClassObjectInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaClassObjectInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassObjectReference

        val actual = javaClassObjectInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#reflectedType") {
      it("should return a profile wrapper around the reference type represented by the class") {
        val expected = mock[ReferenceTypeInfo]
        val referenceType = mock[ReferenceType]

        (mockClassObjectReference.reflectedType _).expects()
          .returning(referenceType).once()

        val mockTypeInfoProfile = mock[TypeInfo]
        mockNewTypeProfile.expects(referenceType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toReferenceType _).expects()
          .returning(expected).once()

        val actual = javaClassObjectInfoProfile.reflectedType

        actual should be (expected)
      }
    }
  }
}
