package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.wrappers.ReferenceTypeWrapper
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureGrabInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
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
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockClassManager = mock[ClassManager]
  private val pureGrabInfoProfile = new PureGrabInfoProfile {
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
    override protected val infoProducer: InfoProducerProfile = mockInfoProducerProfile
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine
    override protected val classManager: ClassManager = mockClassManager

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfoProfile = mockNewThreadProfile(threadReference)

    override protected def newThreadGroupProfile(
      threadGroupReference: ThreadGroupReference
    ): ThreadGroupInfoProfile = mockNewThreadGroupProfile(threadGroupReference)

    override protected def newObjectProfile(
      objectReference: ObjectReference
    ): ObjectInfoProfile = mockNewObjectProfile(objectReference)

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
    describe("#`object`") {
      it("should return a pure object info profile wrapping the object") {
        val expected = mock[ObjectInfoProfile]
        val mockObjectReference = mock[ObjectReference]

        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected).once()

        val actual = pureGrabInfoProfile.`object`(mockObjectReference)

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
