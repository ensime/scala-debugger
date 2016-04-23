package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{ObjectReference, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.wrappers.ReferenceTypeWrapper
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ReferenceTypeInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureGrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewObjectProfile = mockFunction[ThreadReference, ObjectReference, ObjectInfoProfile]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfoProfile]
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

    override protected def newObjectProfile(
      threadReference: ThreadReference,
      objectReference: ObjectReference
    ): ObjectInfoProfile = mockNewObjectProfile(threadReference, objectReference)

    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)
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
  }
}
