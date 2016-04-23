package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureFrameInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewLocalVariableProfile = mockFunction[LocalVariable, Int, IndexedVariableInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfoProfile]
  private val mockNewLocationProfile = mockFunction[Location, LocationInfoProfile]
  private val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]

  private val TestFrameIndex = 999
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockStackFrame = mock[StackFrame]
  private val pureFrameInfoProfile = new PureFrameInfoProfile(
    mockScalaVirtualMachine,
    mockStackFrame,
    TestFrameIndex
  ) {
    override protected def newLocalVariableProfile(
      localVariable: LocalVariable, offsetIndex: Int
    ): IndexedVariableInfoProfile = mockNewLocalVariableProfile(
      localVariable,
      offsetIndex
    )

    override protected def newObjectProfile(
      objectReference: ObjectReference
    ): ObjectInfoProfile = mockNewObjectProfile(objectReference)

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfoProfile = mockNewThreadProfile(threadReference)

    override protected def newLocationProfile(
      location: Location
    ): LocationInfoProfile = mockNewLocationProfile(location)

    override protected def newValueProfile(value: Value): ValueInfoProfile =
      mockNewValueProfile(value)
  }

  describe("PureFrameInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockStackFrame

        val actual = pureFrameInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#index") {
      it("should return the index of the frame") {
        val expected = TestFrameIndex

        val actual = pureFrameInfoProfile.index

        actual should be (expected)
      }
    }

    describe("#thisObject") {
      it("should return the stack frame's 'this' object wrapped in a profile") {
        val expected = mock[ObjectInfoProfile]
        val mockObjectReference = mock[ObjectReference]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mockObjectReference).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.thisObject
        actual should be (expected)
      }

      it("should use the same cached 'this' object profile") {
        val mockObjectProfile = mock[ObjectInfoProfile]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        pureFrameInfoProfile.thisObject should
          be (pureFrameInfoProfile.thisObject)
      }
    }

    describe("#currentThread") {
      it("should return the stack frame's thread wrapped in a profile") {
        val expected = mock[ThreadInfoProfile]
        val mockThreadReference = mock[ThreadReference]

        // stackFrame.thread() fed into newThreadProfile
        (mockStackFrame.thread _).expects()
          .returning(mockThreadReference).once()

        // New thread profile created once using helper method
        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.currentThread
        actual should be (expected)
      }

      it("Should use the same cached thread profile") {
        val mockThreadProfile = mock[ThreadInfoProfile]

        // stackFrame.thread() fed into newThreadProfile
        (mockStackFrame.thread _).expects()
          .returning(mock[ThreadReference]).once()

        // New thread profile created once using helper method
        mockNewThreadProfile.expects(*).returning(mockThreadProfile).once()

        pureFrameInfoProfile.currentThread should
          be (pureFrameInfoProfile.currentThread)
      }
    }

    describe("#location") {
      it("should return the stack frame's location wrapped in a profile") {
        val expected = mock[LocationInfoProfile]
        val mockLocation = mock[Location]

        // stackFrame.location() fed into newLocationProfile
        (mockStackFrame.location _).expects()
          .returning(mockLocation).once()

        // New location profile created once using helper method
        mockNewLocationProfile.expects(mockLocation)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.location
        actual should be (expected)
      }

      it("Should use the same cached location profile") {
        val mockLocationProfile = mock[LocationInfoProfile]

        // stackFrame.location() fed into newLocationProfile
        (mockStackFrame.location _).expects()
          .returning(mock[Location]).once()

        // New location profile created once using helper method
        mockNewLocationProfile.expects(*).returning(mockLocationProfile).once()

        pureFrameInfoProfile.location should
          be (pureFrameInfoProfile.location)
      }
    }

    describe("#variable") {
      it("should return a local variable wrapped in a profile if it exists") {
        val expected = mock[IndexedVariableInfoProfile]

        val name = "someName"
        val mockLocalVariable = mock[LocalVariable]
        val testOffsetIndex = -1 // No index included here

        // Match found in visible variables
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(mockLocalVariable).once()
        mockNewLocalVariableProfile.expects(mockLocalVariable, testOffsetIndex)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.variable(name)

        actual should be (expected)
      }

      it("should return a field wrapped in a profile in no local variable exists") {
        val expected = mock[VariableInfoProfile]

        val name = "someName"

        // No match found in visible variables, so return null
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // unsafeField used to find object profile field
        (mockObjectProfile.field _).expects(name)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.variable(name)

        actual should be (expected)
      }

      it("should throw a NoSuchElement exception if no local variable or field matches") {
        val name = "someName"

        // No match found in visible variables, so return null
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // unsafeField does not find a field and throws a NoSuchElement exception
        (mockObjectProfile.field _).expects(name)
          .throwing(new NoSuchElementException).once()

        intercept[NoSuchElementException] {
          pureFrameInfoProfile.variable(name)
        }
      }
    }

    describe("#fieldVariables") {
      it("should return a collection of profiles wrapping 'this' object's fields") {
        val expected = Seq(mock[VariableInfoProfile])

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // Field variable profiles are accessed
        (mockObjectProfile.fields _).expects().returning(expected).once()

        val actual = pureFrameInfoProfile.fieldVariables

        actual should be (expected)
      }
    }

    describe("#allVariables") {
      it("should return a combination of local and field variables") {
        val _fieldVariables = Seq(mock[VariableInfoProfile])
        val _localVariables = Seq(mock[IndexedVariableInfoProfile])
        val expected = _localVariables ++ _fieldVariables

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockStackFrame,
          0
        ) {
          override def fieldVariables: Seq[VariableInfoProfile] =
            _fieldVariables
          override def localVariables: Seq[IndexedVariableInfoProfile] =
            _localVariables
        }

        val actual = pureFrameInfoProfile.allVariables

        actual should be (expected)
      }
    }

    describe("#localVariables") {
      it("should return all visible variables wrapped in profiles") {
        val expected = Seq(mock[IndexedVariableInfoProfile])
        val mockLocalVariables = Seq(mock[LocalVariable])

        // Raw local variables accessed from stack frame
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(mockLocalVariables.asJava).once()

        // Converted into profiles
        mockLocalVariables.zip(expected).zipWithIndex.foreach { case ((lv, e), i) =>
          mockNewLocalVariableProfile.expects(lv, i).returning(e).once()
        }

        val actual = pureFrameInfoProfile.localVariables

        actual should be (expected)
      }
    }

    describe("#argumentValues") {
      it("should return value profiles for the arguments in the frame") {
        val expected = Seq(mock[ValueInfoProfile])
        val values = expected.map(_ => mock[Value])

        import scala.collection.JavaConverters._
        (mockStackFrame.getArgumentValues _).expects()
          .returning(values.asJava).once()

        expected.zip(values).foreach { case (e, v) =>
          mockNewValueProfile.expects(v).returning(e).once()
        }

        val actual = pureFrameInfoProfile.argumentValues

        actual should be (expected)
      }
    }

    describe("#nonArgumentLocalVariables") {
      it("should return only non-argument visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfoProfile])
        val other = Seq(mock[IndexedVariableInfoProfile])

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def localVariables: Seq[IndexedVariableInfoProfile] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(false).once())
        other.foreach(v => (v.isArgument _).expects().returning(true).once())

        val actual = pureFrameInfoProfile.nonArgumentLocalVariables

        actual should be (expected)
      }
    }

    describe("#argumentLocalVariables") {
      it("should return only argument visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfoProfile])
        val other = Seq(mock[IndexedVariableInfoProfile])

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def localVariables: Seq[IndexedVariableInfoProfile] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(true).once())
        other.foreach(v => (v.isArgument _).expects().returning(false).once())

        val actual = pureFrameInfoProfile.argumentLocalVariables

        actual should be (expected)
      }
    }
  }
}
