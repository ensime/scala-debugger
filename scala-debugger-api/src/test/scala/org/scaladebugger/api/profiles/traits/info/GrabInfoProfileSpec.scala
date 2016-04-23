package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.{ObjectReference, ThreadReference}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestGrabInfoProfile

import scala.util.Success

class GrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("GrabInfoProfile") {
    describe("#tryObject(threadInfo, objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfoProfile, ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            threadInfo: ThreadInfoProfile,
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(
            threadInfo,
            objectReference
          )
        }

        val a1 = mock[ThreadInfoProfile]
        val a2 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        grabInfoProfile.tryObject(a1, a2).get should be (r)
      }
    }

    describe("#object(threadInfo, objectReference)") {
      it("should invoke `object`(threadReference, objectReference)") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            threadReference: ThreadReference,
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(
            threadReference,
            objectReference
          )
        }

        val a1 = mock[ThreadInfoProfile]
        val a2 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]

        val b1 = mock[ThreadReference]
        (a1.toJdiInstance _).expects().returning(b1).once()

        mockUnsafeMethod.expects(b1, a2).returning(r).once()
        grabInfoProfile.tryObject(a1, a2).get should be (r)
      }
    }

    describe("#tryObject(threadReference, objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            threadReference: ThreadReference,
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(
            threadReference,
            objectReference
          )
        }

        val a1 = mock[ThreadReference]
        val a2 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        grabInfoProfile.tryObject(a1, a2).get should be (r)
      }
    }

    describe("#tryThread(threadId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadId: Long): ThreadInfoProfile =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#tryThread(threadReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadReference: ThreadReference): ThreadInfoProfile =
            mockUnsafeMethod(threadReference)
        }

        val a1 = mock[ThreadReference]
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#tryClasses") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ReferenceTypeInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ReferenceTypeInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryClasses.get should be (r)
      }
    }
  }
}
