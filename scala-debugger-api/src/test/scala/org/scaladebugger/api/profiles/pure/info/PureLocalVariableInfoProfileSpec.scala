package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, InfoProducerProfile, TypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMiscInfoProfileTrait

class PureLocalVariableInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockFrameInfoProfile = mock[FrameInfoProfile]
  private val mockLocalVariable = mock[LocalVariable]
  private val TestOffsetIndex = 999
  private val pureLocalVariableInfoProfile = new PureLocalVariableInfoProfile(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockFrameInfoProfile,
    mockLocalVariable,
    TestOffsetIndex
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureLocalVariableInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocalVariable

        val actual = pureLocalVariableInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the local variable's name") {
        val expected = "someName"

        (mockLocalVariable.name _).expects().returning(expected).once()

        val actual = pureLocalVariableInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#typeName") {
      it("should return the local variable's type name") {
        val expected = "some.type.name"

        (mockLocalVariable.typeName _).expects().returning(expected).once()

        val actual = pureLocalVariableInfoProfile.typeName

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfoProfile]

        val mockType = mock[Type]
        (mockLocalVariable.`type` _).expects().returning(mockType).once()

        mockNewTypeProfile.expects(mockType)
          .returning(expected).once()

        val actual = pureLocalVariableInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#isLocal") {
      it("should return true") {
        val expected = true

        val actual = pureLocalVariableInfoProfile.isLocal

        actual should be (expected)
      }
    }

    describe("#isArgument") {
      it("should return true if is an argument") {
        val expected = true

        (mockLocalVariable.isArgument _).expects().returning(true).once()

        val actual = pureLocalVariableInfoProfile.isArgument

        actual should be (expected)
      }

      it("should return false if is not an argument") {
        val expected = false

        (mockLocalVariable.isArgument _).expects().returning(false).once()

        val actual = pureLocalVariableInfoProfile.isArgument

        actual should be (expected)
      }
    }

    describe("#isField") {
      it("should return false") {
        val expected = false

        val actual = pureLocalVariableInfoProfile.isField

        actual should be (expected)
      }
    }


    describe("#setValueFromInfo") {
      it("should be able to set the value using the info") {
        val expected = mock[ValueInfoProfile]

        // Retrieves JDI stack frame to set value
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Retrieves JDI value from info
        val mockStringReference = mock[StringReference]
        (expected.toJdiInstance _).expects()
          .returning(mockStringReference).once()

        // Ensure setting the value on the stack frame is verified
        (mockStackFrame.setValue _)
          .expects(mockLocalVariable, mockStringReference)
          .once()

        pureLocalVariableInfoProfile.setValueFromInfo(expected) should be (expected)
      }
    }

    describe("#toValueInfo") {
      it("should return a wrapper around the value of the localVariable") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        // Retrieve the underlying stack frame
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Retrieve the value from the stack frame
        (mockStackFrame.getValue _).expects(mockLocalVariable)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
        val pureLocalVariableInfoProfile = new PureLocalVariableInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockFrameInfoProfile,
          mockLocalVariable,
          TestOffsetIndex
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureLocalVariableInfoProfile.toValueInfo should be (expected)
      }
    }
  }
}
