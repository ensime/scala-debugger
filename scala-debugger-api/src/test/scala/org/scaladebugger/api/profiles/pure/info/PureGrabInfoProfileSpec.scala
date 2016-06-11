package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.wrappers.ReferenceTypeWrapper
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureGrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewObjectProfile = mockFunction[ThreadReference, ObjectReference, ObjectInfoProfile]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfoProfile]
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfoProfile]
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
  private val mockNewLocationProfile = mockFunction[Location, LocationInfoProfile]
  private val mockNewMethodProfile = mockFunction[Method, MethodInfoProfile]
  private val mockNewFrameProfile = mockFunction[StackFrame, FrameInfoProfile]
  private val mockNewFieldProfileFromRef = mockFunction[ReferenceType, Field, FieldVariableInfoProfile]
  private val mockNewFieldProfileFromObj = mockFunction[ObjectReference, Field, FieldVariableInfoProfile]
  private val mockNewLocalVariableProfile = mockFunction[StackFrame, LocalVariable, VariableInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockClassManager = mock[ClassManager]
  private val pureGrabInfoProfile = new PureGrabInfoProfile {
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine
    override protected val classManager: ClassManager = mockClassManager

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfoProfile = mockNewThreadProfile(threadReference)

    override protected def newThreadGroupProfile(
      threadGroupReference: ThreadGroupReference
    ): ThreadGroupInfoProfile = mockNewThreadGroupProfile(threadGroupReference)

    override protected def newObjectProfile(
      threadReference: ThreadReference,
      objectReference: ObjectReference
    ): ObjectInfoProfile = mockNewObjectProfile(threadReference, objectReference)

    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)

    override protected def newLocalVariableProfile(
      stackFrame: StackFrame,
      localVariable: LocalVariable
    ): VariableInfoProfile = mockNewLocalVariableProfile(stackFrame, localVariable)

    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)

    override protected def newValueProfile(value: Value): ValueInfoProfile =
      mockNewValueProfile(value)

    override protected def newLocationProfile(
      location: Location
    ): LocationInfoProfile = mockNewLocationProfile(location)

    override protected def newMethodProfile(method: Method): MethodInfoProfile =
      mockNewMethodProfile(method)

    override protected def newFrameProfile(
      stackFrame: StackFrame
    ): FrameInfoProfile = mockNewFrameProfile(stackFrame)

    override protected def newFieldProfile(
      objectReference: ObjectReference,
      field: Field
    ): FieldVariableInfoProfile = mockNewFieldProfileFromObj(objectReference, field)

    override protected def newFieldProfile(
      referenceType: ReferenceType,
      field: Field
    ): FieldVariableInfoProfile = mockNewFieldProfileFromRef(referenceType, field)
  }

  describe("PureGrabInfoProfile") {
    describe("#`object`(threadReference, objectReference)") {
      it("should return a pure object info profile wrapping the thread and object") {
        val expected = mock[ObjectInfoProfile]
        val mockThreadReference = mock[ThreadReference]
        val mockObjectReference = mock[ObjectReference]

        mockNewObjectProfile.expects(mockThreadReference, mockObjectReference)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.`object`(
          mockThreadReference,
          mockObjectReference
        )

        actual should be (expected)
      }
    }

    describe("#threads") {
      it("should return a collection of profiles wrapping class reference types") {
        val expected = Seq(mock[ThreadInfoProfile])
        val referenceTypes = Seq(mock[ThreadReference])

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allThreads _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewThreadProfile.expects(r).returning(e).once()
        }

        val actual = pureGrabInfoProfile.threads

        actual should be (expected)
      }
    }

    describe("#thread(threadReference)") {
      it("should return a pure thread info profile wrapping the thread") {
        val expected = mock[ThreadInfoProfile]
        val mockThreadReference = mock[ThreadReference]

        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.thread(mockThreadReference)

        actual should be (expected)
      }
    }

    describe("#threadOption(threadId)") {
      it("should return Some(profile) if a thread with matching unique id is found") {
        val expected = Some(mock[ThreadInfoProfile])
        val mockThreadReference = mock[ThreadReference]
        val threadId = 999L

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allThreads _).expects()
          .returning(Seq(mockThreadReference).asJava).once()

        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected.get).once()

        (expected.get.uniqueId _).expects().returning(threadId).once()

        val actual = pureGrabInfoProfile.threadOption(threadId)

        actual should be (expected)
      }

      it("should return None if no thread with a matching unique id is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfoProfile]
        val mockThreadReference = mock[ThreadReference]

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allThreads _).expects()
          .returning(Seq(mockThreadReference).asJava).once()

        mockNewThreadProfile.expects(mockThreadReference)
          .returning(mockThreadInfo).once()

        (mockThreadInfo.uniqueId _).expects().returning(998L).once()
        val actual = pureGrabInfoProfile.threadOption(999L)

        actual should be (expected)
      }
    }

    describe("#threadGroups") {
      it("should return a collection of profiles wrapping class reference types") {
        val expected = Seq(mock[ThreadGroupInfoProfile])
        val referenceTypes = Seq(mock[ThreadGroupReference])

        import scala.collection.JavaConverters._
        (mockVirtualMachine.topLevelThreadGroups _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewThreadGroupProfile.expects(r).returning(e).once()
        }

        val actual = pureGrabInfoProfile.threadGroups

        actual should be (expected)
      }
    }

    describe("#threadGroup(threadGroupReference)") {
      it("should return a pure threadGroup info profile wrapping the threadGroup") {
        val expected = mock[ThreadGroupInfoProfile]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.threadGroup(mockThreadGroupReference)

        actual should be (expected)
      }
    }

    describe("#threadGroupOption(threadGroupId)") {
      it("should return Some(profile) if a threadGroup with matching unique id is found") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val mockThreadGroupReference = mock[ThreadGroupReference]
        val threadGroupId = 999L

        import scala.collection.JavaConverters._
        (mockVirtualMachine.topLevelThreadGroups _).expects()
          .returning(Seq(mockThreadGroupReference).asJava).once()

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(expected.get).once()

        (expected.get.uniqueId _).expects().returning(threadGroupId).once()

        val actual = pureGrabInfoProfile.threadGroupOption(threadGroupId)

        actual should be (expected)
      }

      it("should recurse through each thread group's subgroups to find a match") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        import scala.collection.JavaConverters._
        (mockVirtualMachine.topLevelThreadGroups _).expects()
          .returning(Seq(mockThreadGroupReference).asJava).once()

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(mockThreadGroupInfo).once()

        (mockThreadGroupInfo.uniqueId _).expects().returning(998L).once()

        (mockThreadGroupInfo.threadGroups _).expects()
          .returning(Seq(expected.get)).once()

        (expected.get.uniqueId _).expects().returning(999L).once()

        val actual = pureGrabInfoProfile.threadGroupOption(999L)

        actual should be (expected)
      }

      it("should return None if no threadGroup with a matching unique id is found") {
        val expected = None
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        import scala.collection.JavaConverters._
        (mockVirtualMachine.topLevelThreadGroups _).expects()
          .returning(Seq(mockThreadGroupReference).asJava).once()

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(mockThreadGroupInfo).once()

        (mockThreadGroupInfo.uniqueId _).expects().returning(998L).once()

        (mockThreadGroupInfo.threadGroups _).expects().returning(Nil).once()

        val actual = pureGrabInfoProfile.threadGroupOption(999L)

        actual should be (expected)
      }
    }

    describe("#classes") {
      it("should return a collection of profiles wrapping class reference types") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureGrabInfoProfile.classes

        actual should be (expected)
      }
    }

    describe("#classOption(name)") {
      it("should return Some(profile) if a class with matching name is found") {
        val expected = Some(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])
        val name = "some.class.name"

        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        mockNewReferenceTypeProfile.expects(referenceTypes.head)
          .returning(expected.get).once()

        (expected.get.name _).expects().returning(name).once()

        val actual = pureGrabInfoProfile.classOption(name)

        actual should be (expected)
      }

      it("should return None if no class with a matching name is found") {
        val expected = None
        val referenceTypes = Seq(mock[ReferenceType])
        val name = "some.class.name"

        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        val mockReferenceTypeInfo = mock[ReferenceTypeInfoProfile]
        mockNewReferenceTypeProfile.expects(referenceTypes.head)
          .returning(mockReferenceTypeInfo).once()

        (mockReferenceTypeInfo.name _).expects().returning(name + 1).once()

        val actual = pureGrabInfoProfile.classOption(name)

        actual should be (expected)
      }
    }

    describe("#`class`") {
      it("should return a reference type info profile wrapping the reference type") {
        val expected = mock[ReferenceTypeInfoProfile]
        val mockReferenceType = mock[ReferenceType]

        mockNewReferenceTypeProfile.expects(mockReferenceType)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.`class`(mockReferenceType)

        actual should be (expected)
      }
    }

    describe("#location") {
      it("should return a location info profile wrapping the location") {
        val expected = mock[LocationInfoProfile]
        val mockLocation = mock[Location]

        mockNewLocationProfile.expects(mockLocation)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.location(mockLocation)

        actual should be (expected)
      }
    }

    describe("#`type`") {
      it("should return a type info profile wrapping the type") {
        val expected = mock[TypeInfoProfile]
        val mockType = mock[Type]

        mockNewTypeProfile.expects(mockType)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.`type`(mockType)

        actual should be (expected)
      }
    }

    describe("#field(reference type, field)") {
      it("should return a variable info profile wrapping the field") {
        val expected = mock[FieldVariableInfoProfile]
        val mockReferenceType = mock[ReferenceType]
        val mockField = mock[Field]

        mockNewFieldProfileFromRef.expects(mockReferenceType, mockField)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.field(mockReferenceType, mockField)

        actual should be (expected)
      }
    }

    describe("#field(object reference, field)") {
      it("should return a variable info profile wrapping the field") {
        val expected = mock[FieldVariableInfoProfile]
        val mockObjectReference = mock[ObjectReference]
        val mockField = mock[Field]

        mockNewFieldProfileFromObj.expects(mockObjectReference, mockField)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.field(mockObjectReference, mockField)

        actual should be (expected)
      }
    }

    describe("#localVariable") {
      it("should return a variable info profile wrapping the local variable") {
        val expected = mock[VariableInfoProfile]
        val mockStackFrame = mock[StackFrame]
        val mockLocalVariable = mock[LocalVariable]

        mockNewLocalVariableProfile.expects(mockStackFrame, mockLocalVariable)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.localVariable(
          mockStackFrame,
          mockLocalVariable
        )

        actual should be (expected)
      }
    }

    describe("#stackFrame") {
      it("should return a frame info profile wrapping the stack frame") {
        val expected = mock[FrameInfoProfile]
        val mockStackFrame = mock[StackFrame]

        mockNewFrameProfile.expects(mockStackFrame)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.stackFrame(mockStackFrame)

        actual should be (expected)
      }
    }

    describe("#method") {
      it("should return a method info profile wrapping the method") {
        val expected = mock[MethodInfoProfile]
        val mockMethod = mock[Method]

        mockNewMethodProfile.expects(mockMethod)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.method(mockMethod)

        actual should be (expected)
      }
    }

    describe("#value") {
      it("should return a value info profile wrapping the value") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        mockNewValueProfile.expects(mockValue)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.value(mockValue)

        actual should be (expected)
      }
    }
  }
}
