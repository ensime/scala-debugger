package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMiscInfoTrait

class PureLocalVariableInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockFrameInfoProfile = mock[FrameInfo]
  private val mockLocalVariable = mock[LocalVariable]
  private val TestOffsetIndex = 999
  private val pureLocalVariableInfoProfile = new PureLocalVariableInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockFrameInfoProfile,
    mockLocalVariable,
    TestOffsetIndex
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("PureLocalVariableInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[IndexedVariableInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newLocalVariableInfoProfile(
          _: ScalaVirtualMachine,
          _: FrameInfo,
          _: LocalVariable,
          _: Int
        )(
          _: VirtualMachine
        )).expects(
          mockScalaVirtualMachine,
          mockFrameInfoProfile,
          mockLocalVariable,
          TestOffsetIndex,
          *
        ).returning(expected).once()

        val actual = pureLocalVariableInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureLocalVariableInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

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
        val expected = mock[TypeInfo]

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
        val expected = mock[ValueInfo]

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
        val expected = mock[ValueInfo]
        val mockValue = mock[Value]

        // Retrieve the underlying stack frame
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Retrieve the value from the stack frame
        (mockStackFrame.getValue _).expects(mockLocalVariable)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfo]
        val pureLocalVariableInfoProfile = new PureLocalVariableInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockFrameInfoProfile,
          mockLocalVariable,
          TestOffsetIndex
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfo =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureLocalVariableInfoProfile.toValueInfo should be (expected)
      }
    }
  }
}
