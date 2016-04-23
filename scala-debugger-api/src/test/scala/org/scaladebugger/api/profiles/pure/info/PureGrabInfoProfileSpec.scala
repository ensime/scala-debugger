package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{ObjectReference, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.wrappers.ReferenceTypeWrapper
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ReferenceTypeInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureGrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewObjectProfile = mockFunction[ThreadReference, ObjectReference, ObjectInfoProfile]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val pureGrabInfoProfile = new PureGrabInfoProfile {
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine

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

    describe("#thread(threadReference)") {
      it("should return a pure thread info profile wrapping the thread") {
        val expected = mock[ThreadReference]

        (expected.referenceType _).expects().returning(mock[ReferenceType]).once()
        val actual = pureGrabInfoProfile.thread(expected)

        (expected.uniqueID _).expects().returning(999L).twice()
        actual.uniqueId should be (expected.uniqueID())
      }
    }

    describe("#thread(threadId)") {
      it("should return a new profile if a thread with matching unique id is found") {
        val expected = mock[ThreadReference]

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allThreads _).expects()
          .returning(Seq(expected).asJava).once()

        (expected.uniqueID _).expects().returning(999L).repeated(3).times()
        (expected.referenceType _).expects().returning(mock[ReferenceType]).once()
        val actual = pureGrabInfoProfile.thread(999L)

        actual.uniqueId should be (expected.uniqueID())
      }

      it("should throw an exception if no thread with a matching unique id is found") {
        val mockThreadReference = mock[ThreadReference]

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allThreads _).expects()
          .returning(Seq(mockThreadReference).asJava).once()

        intercept[NoSuchElementException] {
          (mockThreadReference.uniqueID _).expects().returning(998L).once()
          pureGrabInfoProfile.thread(999L)
        }
      }
    }

    describe("#classes") {
      it("should return a collection of profiles wrapping class reference types") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockVirtualMachine.allClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureGrabInfoProfile.classes

        actual should be (expected)
      }
    }
  }
}
