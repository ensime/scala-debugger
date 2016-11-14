package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureClassObjectInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockClassObjectReference = mock[ClassObjectReference]
  private val pureClassObjectInfoProfile = new PureClassObjectInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockClassObjectReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureClassObjectInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassObjectReference

        val actual = pureClassObjectInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#reflectedType") {
      it("should return a profile wrapper around the reference type represented by the class") {
        val expected = mock[ReferenceTypeInfoProfile]
        val referenceType = mock[ReferenceType]

        (mockClassObjectReference.reflectedType _).expects()
          .returning(referenceType).once()

        val mockTypeInfoProfile = mock[TypeInfoProfile]
        mockNewTypeProfile.expects(referenceType)
          .returning(mockTypeInfoProfile).once()

        (mockTypeInfoProfile.toReferenceType _).expects()
          .returning(expected).once()

        val actual = pureClassObjectInfoProfile.reflectedType

        actual should be (expected)
      }
    }
  }
}
