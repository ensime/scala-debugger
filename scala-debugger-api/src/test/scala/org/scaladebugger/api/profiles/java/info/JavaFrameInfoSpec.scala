package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaFrameInfoSpec extends ParallelMockFunSpec
{
  private val mockNewLocalVariableProfile = mockFunction[LocalVariable, Int, IndexedVariableInfo]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfo]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfo]
  private val mockNewLocationProfile = mockFunction[Location, LocationInfo]
  private val mockNewValueProfile = mockFunction[Value, ValueInfo]

  private val TestFrameIndex = 999
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockStackFrame = mock[StackFrame]
  private val javaFrameInfoProfile = new JavaFrameInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockStackFrame,
    TestFrameIndex
  ) {
    override protected def newLocalVariableProfile(
      localVariable: LocalVariable, offsetIndex: Int
    ): IndexedVariableInfo = mockNewLocalVariableProfile(
      localVariable,
      offsetIndex
    )

    override protected def newObjectProfile(
      objectReference: ObjectReference
    ): ObjectInfo = mockNewObjectProfile(objectReference)

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfo = mockNewThreadProfile(threadReference)

    override protected def newLocationProfile(
      location: Location
    ): LocationInfo = mockNewLocationProfile(location)

    override protected def newValueProfile(value: Value): ValueInfo =
      mockNewValueProfile(value)
  }

  describe("JavaFrameInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[FrameInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newFrameInfo _)
          .expects(mockScalaVirtualMachine, mockStackFrame, TestFrameIndex)
          .returning(expected).once()

        val actual = javaFrameInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaFrameInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockStackFrame

        val actual = javaFrameInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#index") {
      it("should return the index of the frame") {
        val expected = TestFrameIndex

        val actual = javaFrameInfoProfile.index

        actual should be (expected)
      }
    }

    describe("#thisObjectOption") {
      it("should return Some stack frame's 'this' object wrapped in a profile") {
        val expected = Some(mock[ObjectInfo])
        val mockObjectReference = mock[ObjectReference]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mockObjectReference).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected.get).once()

        val actual = javaFrameInfoProfile.thisObjectOption
        actual should be (expected)
      }

      it("should use the same cached 'this' object profile") {
        val mockObjectProfile = mock[ObjectInfo]

        // stackFrame.thisObject() fed into newObjectProfile
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()

        // New object profile created once using helper method
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        javaFrameInfoProfile.thisObjectOption should
          be (javaFrameInfoProfile.thisObjectOption)
      }

      it("should return None if 'this' object is unavailable") {
        val expected = None

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = javaFrameInfoProfile.thisObjectOption

        actual should be (expected)
      }
    }

    describe("#currentThread") {
      it("should return the stack frame's thread wrapped in a profile") {
        val expected = mock[ThreadInfo]
        val mockThreadReference = mock[ThreadReference]

        // stackFrame.thread() fed into newThreadProfile
        (mockStackFrame.thread _).expects()
          .returning(mockThreadReference).once()

        // New thread profile created once using helper method
        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected).once()

        val actual = javaFrameInfoProfile.currentThread
        actual should be (expected)
      }

      it("Should use the same cached thread profile") {
        val mockThreadProfile = mock[ThreadInfo]

        // stackFrame.thread() fed into newThreadProfile
        (mockStackFrame.thread _).expects()
          .returning(mock[ThreadReference]).once()

        // New thread profile created once using helper method
        mockNewThreadProfile.expects(*).returning(mockThreadProfile).once()

        javaFrameInfoProfile.currentThread should
          be (javaFrameInfoProfile.currentThread)
      }
    }

    describe("#location") {
      it("should return the stack frame's location wrapped in a profile") {
        val expected = mock[LocationInfo]
        val mockLocation = mock[Location]

        // stackFrame.location() fed into newLocationProfile
        (mockStackFrame.location _).expects()
          .returning(mockLocation).once()

        // New location profile created once using helper method
        mockNewLocationProfile.expects(mockLocation)
          .returning(expected).once()

        val actual = javaFrameInfoProfile.location
        actual should be (expected)
      }

      it("Should use the same cached location profile") {
        val mockLocationProfile = mock[LocationInfo]

        // stackFrame.location() fed into newLocationProfile
        (mockStackFrame.location _).expects()
          .returning(mock[Location]).once()

        // New location profile created once using helper method
        mockNewLocationProfile.expects(*).returning(mockLocationProfile).once()

        javaFrameInfoProfile.location should
          be (javaFrameInfoProfile.location)
      }
    }

    describe("#variableOption") {
      it("should return Some local variable wrapped in a profile if it exists") {
        val expected = Some(mock[IndexedVariableInfo])

        val name = "someName"
        val mockLocalVariable = mock[LocalVariable]
        val testOffsetIndex = -1 // No index included here

        // Match found in visible variables
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(mockLocalVariable).once()
        mockNewLocalVariableProfile.expects(mockLocalVariable, testOffsetIndex)
          .returning(expected.get).once()

        val actual = javaFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }

      it("should return Some field wrapped in a profile in no local variable exists") {
        val expected = Some(mock[FieldVariableInfo])

        val name = "someName"

        // No match found in visible variables, so return null
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.fieldOption _).expects(name)
          .returning(expected).once()

        val actual = javaFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }

      it("should return None if no local variable or field matches") {
        val expected = None
        val name = "someName"

        // No match found in visible variables, so return null
        (mockStackFrame.visibleVariableByName _).expects(name)
          .returning(null).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.fieldOption _).expects(name).returning(None).once()

        val actual = javaFrameInfoProfile.variableOption(name)

        actual should be (expected)
      }
    }

    describe("#fieldVariables") {
      it("should return a collection of profiles wrapping 'this' object's fields") {
        val expected = Seq(mock[FieldVariableInfo])

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // Field variable profiles are accessed
        (mockObjectProfile.fields _).expects().returning(expected).once()

        val actual = javaFrameInfoProfile.fieldVariables

        actual should be (expected)
      }

      it("should return an empty collection if 'this' object is unavailable") {
        val expected = Nil

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = javaFrameInfoProfile.fieldVariables

        actual should be (expected)
      }
    }

    describe("#allVariables") {
      it("should return a combination of local and field variables") {
        val _fieldVariables = Seq(mock[FieldVariableInfo])
        val _localVariables = Seq(mock[IndexedVariableInfo])
        val expected = _localVariables ++ _fieldVariables

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          0
        ) {
          override def fieldVariables: Seq[FieldVariableInfo] =
            _fieldVariables
          override def localVariables: Seq[IndexedVariableInfo] =
            _localVariables
        }

        val actual = javaFrameInfoProfile.allVariables

        actual should be (expected)
      }
    }

    describe("#localVariables") {
      it("should return all visible variables wrapped in profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val mockLocalVariables = Seq(mock[LocalVariable])

        // Raw local variables accessed from stack frame
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(mockLocalVariables.asJava).once()

        // Converted into profiles
        mockLocalVariables.zip(expected).foreach { case (lv, e) =>
          mockNewLocalVariableProfile.expects(lv, -1).returning(e).once()
        }

        val actual = javaFrameInfoProfile.localVariables

        actual should be (expected)
      }
    }

    describe("#argumentValues") {
      it("should return value profiles for the arguments in the frame") {
        val expected = Seq(mock[ValueInfo])
        val values = expected.map(_ => mock[Value])

        import scala.collection.JavaConverters._
        (mockStackFrame.getArgumentValues _).expects()
          .returning(values.asJava).once()

        expected.zip(values).foreach { case (e, v) =>
          mockNewValueProfile.expects(v).returning(e).once()
        }

        val actual = javaFrameInfoProfile.argumentValues

        actual should be (expected)
      }
    }

    describe("#nonArgumentLocalVariables") {
      it("should return only non-argument visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val other = Seq(mock[IndexedVariableInfo])

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def localVariables: Seq[IndexedVariableInfo] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(false).once())
        other.foreach(v => (v.isArgument _).expects().returning(true).once())

        val actual = javaFrameInfoProfile.nonArgumentLocalVariables

        actual should be (expected)
      }
    }

    describe("#argumentLocalVariables") {
      it("should return only argument visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val other = Seq(mock[IndexedVariableInfo])

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def localVariables: Seq[IndexedVariableInfo] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(true).once())
        other.foreach(v => (v.isArgument _).expects().returning(false).once())

        val actual = javaFrameInfoProfile.argumentLocalVariables

        actual should be (expected)
      }
    }

    describe("#indexedVariableOption") {
      it("should return Some local variable wrapped in a profile if it exists") {
        val expected = Some(mock[IndexedVariableInfo])

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

        val actual = javaFrameInfoProfile.indexedVariableOption(name)

        actual should be (expected)
      }

      it("should return Some field wrapped in a profile in no local variable exists") {
        val expected = Some(mock[FieldVariableInfo])

        val name = "someName"

        // No match found in visible variables, so return Nil
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(Seq[LocalVariable]().asJava).once()

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.indexedFieldOption _).expects(name)
          .returning(expected).once()

        val actual = javaFrameInfoProfile.indexedVariableOption(name)

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
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        (mockObjectProfile.indexedFieldOption _).expects(name)
          .returning(None).once()

        val actual = javaFrameInfoProfile.indexedVariableOption(name)

        actual should be (expected)
      }
    }

    describe("#indexedFieldVariables") {
      it("should return a collection of profiles wrapping 'this' object's fields") {
        val expected = Seq(mock[FieldVariableInfo])

        // 'this' object profile is created and used
        val mockObjectProfile = mock[ObjectInfo]
        (mockStackFrame.thisObject _).expects()
          .returning(mock[ObjectReference]).once()
        mockNewObjectProfile.expects(*).returning(mockObjectProfile).once()

        // Field variable profiles are accessed
        (mockObjectProfile.indexedFields _).expects().returning(expected).once()

        val actual = javaFrameInfoProfile.indexedFieldVariables

        actual should be (expected)
      }

      it("should return an empty collection if 'this' object is unavailable") {
        val expected = Nil

        (mockStackFrame.thisObject _).expects().returning(null).once()

        val actual = javaFrameInfoProfile.indexedFieldVariables

        actual should be (expected)
      }
    }

    describe("#indexedAllVariables") {
      it("should return a combination of local and field indexed variables") {
        val _fieldVariables = Seq(mock[FieldVariableInfo])
        val _localVariables = Seq(mock[IndexedVariableInfo])
        val expected = _localVariables ++ _fieldVariables

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          0
        ) {
          override def indexedFieldVariables: Seq[FieldVariableInfo] =
            _fieldVariables
          override def indexedLocalVariables: Seq[IndexedVariableInfo] =
            _localVariables
        }

        val actual = javaFrameInfoProfile.indexedAllVariables

        actual should be (expected)
      }
    }

    describe("#indexedLocalVariables") {
      it("should return all visible variables wrapped in profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val mockLocalVariables = Seq(mock[LocalVariable])

        // Raw local variables accessed from stack frame
        import scala.collection.JavaConverters._
        (mockStackFrame.visibleVariables _).expects()
          .returning(mockLocalVariables.asJava).once()

        // Converted into profiles
        mockLocalVariables.zip(expected).zipWithIndex.foreach { case ((lv, e), i) =>
          mockNewLocalVariableProfile.expects(lv, i).returning(e).once()
        }

        val actual = javaFrameInfoProfile.indexedLocalVariables

        actual should be (expected)
      }
    }

    describe("#indexedNonArgumentLocalVariables") {
      it("should return only non-argument indexed visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val other = Seq(mock[IndexedVariableInfo])

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def indexedLocalVariables: Seq[IndexedVariableInfo] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(false).once())
        other.foreach(v => (v.isArgument _).expects().returning(true).once())

        val actual = javaFrameInfoProfile.indexedNonArgumentLocalVariables

        actual should be (expected)
      }
    }

    describe("#indexedArgumentLocalVariables") {
      it("should return only argument indexed visible variable profiles") {
        val expected = Seq(mock[IndexedVariableInfo])
        val other = Seq(mock[IndexedVariableInfo])

        val javaFrameInfoProfile = new JavaFrameInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStackFrame,
          TestFrameIndex
        ) {
          override def indexedLocalVariables: Seq[IndexedVariableInfo] =
            expected ++ other
        }

        expected.foreach(v => (v.isArgument _).expects().returning(true).once())
        other.foreach(v => (v.isArgument _).expects().returning(false).once())

        val actual = javaFrameInfoProfile.indexedArgumentLocalVariables

        actual should be (expected)
      }
    }
  }
}
