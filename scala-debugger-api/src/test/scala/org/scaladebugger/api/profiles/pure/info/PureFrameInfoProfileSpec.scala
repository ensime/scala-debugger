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
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockStackFrame = mock[StackFrame]
  private val pureFrameInfoProfile = new PureFrameInfoProfile(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
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

    describe("#thisObjectOption") {
      it("should return Some stack frame's 'this' object wrapped in a profile") {
        val expected = Some(mock[ObjectInfoProfile])
        val mockObjectReference = mock[ObjectReference]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mockObjectReference).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected.get).once()

        val actual = pureFrameInfoProfile.thisObjectOption
        actual should be (expected)
      }

      it("should use the same cached 'this' object profile") {
        val mockObjectProfile = mock[ObjectInfoProfile]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        pureFrameInfoProfile.thisObjectOption should
          be (pureFrameInfoProfile.thisObjectOption)
      }

      it("should return None if 'this' object is unavailable") {
        val expected = None

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = pureFrameInfoProfile.thisObjectOption

        actual should be (expected)
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

    describe("#variableOption") {
      it("should return Some local variable wrapped in a profile if it exists") {
        val expected = Some(mock[IndexedVariableInfoProfile])

        val name = "someName"
        val mockLocalVariable = mock[LocalVariable]
        val testOffsetIndex = -1 // No index included here

        // Match found in visible variables
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(mockLocalVariable).once()
        mockNewLocalVariableProfile.expects(mockLocalVariable, testOffsetIndex)
          .returning(expected.get).once()

        val actual = pureFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }

      it("should return Some field wrapped in a profile in no local variable exists") {
        val expected = Some(mock[FieldVariableInfoProfile])

        val name = "someName"

        // No match found in visible variables, so return null
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.fieldOption _).expects(name)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }

      it("should return None if no local variable or field matches") {
        val expected = None
        val name = "someName"

        // No match found in visible variables, so return null
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.fieldOption _).expects(name).returning(None).once()

        val actual = pureFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }
    }

    describe("#fieldVariables") {
      it("should return a collection of profiles wrapping 'this' object's fields") {
        val expected = Seq(mock[FieldVariableInfoProfile])

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

      it("should return an empty collection if 'this' object is unavailable") {
        val expected = Nil

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = pureFrameInfoProfile.fieldVariables

        actual should be (expected)
      }
    }

    describe("#allVariables") {
      it("should return a combination of local and field variables") {
        val _fieldVariables = Seq(mock[FieldVariableInfoProfile])
        val _localVariables = Seq(mock[IndexedVariableInfoProfile])
        val expected = _localVariables ++ _fieldVariables

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          0
        ) {
          override def fieldVariables: Seq[FieldVariableInfoProfile] =
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
        mockLocalVariables.zip(expected).foreach { case (lv, e) =>
          mockNewLocalVariableProfile.expects(lv, -1).returning(e).once()
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
          mockInfoProducerProfile,
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
          mockInfoProducerProfile,
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

    describe("#indexedVariableOption") {
      it("should return Some local variable wrapped in a profile if it exists") {
        val expected = Some(mock[IndexedVariableInfoProfile])

        val name = "someName"
        val mockLocalVariable = mock[LocalVariable]
        val testOffsetIndex = 0

        // Match found in visible variables
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(Seq(mockLocalVariable).asJava).once()
        (expected.get.name _).expects().returning(name).once()

        mockNewLocalVariableProfile.expects(mockLocalVariable, testOffsetIndex)
          .returning(expected.get).once()

        val actual = pureFrameInfoProfile.indexedVariableOption(name)

        actual should be (expected)
      }

      it("should return Some field wrapped in a profile in no local variable exists") {
        val expected = Some(mock[FieldVariableInfoProfile])

        val name = "someName"

        // No match found in visible variables, so return Nil
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(Seq[LocalVariable]().asJava).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.indexedFieldOption _).expects(name)
          .returning(expected).once()

        val actual = pureFrameInfoProfile.indexedVariableOption(name)

        actual should be (expected)
      }

      it("should return None if no local variable or field matches") {
        val expected = None

        val name = "someName"

        // No match found in visible variables, so return Nil
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(Seq[LocalVariable]().asJava).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.indexedFieldOption _).expects(name)
          .returning(None).once()

        val actual = pureFrameInfoProfile.indexedVariableOption(name)

        actual should be (expected)
      }
    }

    describe("#indexedFieldVariables") {
      it("should return a collection of profiles wrapping 'this' object's fields") {
        val expected = Seq(mock[FieldVariableInfoProfile])

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfoProfile]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // Field variable profiles are accessed
        (mockObjectProfile.indexedFields _).expects().returning(expected).once()

        val actual = pureFrameInfoProfile.indexedFieldVariables

        actual should be (expected)
      }

      it("should return an empty collection if 'this' object is unavailable") {
        val expected = Nil

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = pureFrameInfoProfile.indexedFieldVariables

        actual should be (expected)
      }
    }

    describe("#indexedAllVariables") {
      it("should return a combination of local and field indexed variables") {
        val _fieldVariables = Seq(mock[FieldVariableInfoProfile])
        val _localVariables = Seq(mock[IndexedVariableInfoProfile])
        val expected = _localVariables ++ _fieldVariables

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          0
        ) {
          override def indexedFieldVariables: Seq[FieldVariableInfoProfile] =
            _fieldVariables
          override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] =
            _localVariables
        }

        val actual = pureFrameInfoProfile.indexedAllVariables

        actual should be (expected)
      }
    }

    describe("#indexedLocalVariables") {
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

        val actual = pureFrameInfoProfile.indexedLocalVariables

        actual should be (expected)
      }
    }

    describe("#indexedNonArgumentLocalVariables") {
      it("should return only non-argument indexed visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfoProfile])
        val other = Seq(mock[IndexedVariableInfoProfile])

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(false).once())
        other.foreach(v => (v.isArgument _).expects().returning(true).once())

        val actual = pureFrameInfoProfile.indexedNonArgumentLocalVariables

        actual should be (expected)
      }
    }

    describe("#indexedArgumentLocalVariables") {
      it("should return only argument indexed visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfoProfile])
        val other = Seq(mock[IndexedVariableInfoProfile])

        val pureFrameInfoProfile = new PureFrameInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(true).once())
        other.foreach(v => (v.isArgument _).expects().returning(false).once())

        val actual = pureFrameInfoProfile.indexedArgumentLocalVariables

        actual should be (expected)
      }
    }
  }
}
