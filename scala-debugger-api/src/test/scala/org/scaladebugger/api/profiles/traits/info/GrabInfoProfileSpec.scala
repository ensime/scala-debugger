package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.{ObjectReference, ThreadGroupReference, ThreadReference}
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

    describe("#tryThreads") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ThreadInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ThreadInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryThreads.get should be (r)
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

    describe("#thread(threadId)") {
      it("should return the Some result of threadOption(threadId)") {
        val mockUnsafeMethod = mockFunction[Long, Option[ThreadInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(threadId: Long): Option[ThreadInfoProfile] =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.thread(a1) should be (r)
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

    describe("#tryThreadGroups") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ThreadGroupInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ThreadGroupInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryThreadGroups.get should be (r)
      }
    }

    describe("#tryThreadGroup(threadGroupId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(threadGroupId: Long): ThreadGroupInfoProfile =
            mockUnsafeMethod(threadGroupId)
        }

        val a1 = 999L
        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
      }
    }

    describe("#threadGroup(threadGroupId)") {
      it("should return the Some result of threadGroupOption(threadGroupId)") {
        val mockUnsafeMethod = mockFunction[Long, Option[ThreadGroupInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroupOption(
            threadGroupId: Long
          ): Option[ThreadGroupInfoProfile] = mockUnsafeMethod(threadGroupId)
        }

        val a1 = 999L
        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.threadGroup(a1) should be (r)
      }
    }

    describe("#tryThreadGroup(threadGroupReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadGroupReference, ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(
            threadGroupReference: ThreadGroupReference
          ): ThreadGroupInfoProfile = mockUnsafeMethod(threadGroupReference)
        }

        val a1 = mock[ThreadGroupReference]
        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
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

    describe("#tryClass(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ReferenceTypeInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `class`(name: String): ReferenceTypeInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "some.class.name"
        val r = mock[ReferenceTypeInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryClass(a1).get should be (r)
      }
    }

    describe("#`class`(name)") {
      it("should return the Some result of classOption(name)") {
        val mockUnsafeMethod = mockFunction[String, Option[ReferenceTypeInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classOption(name: String): Option[ReferenceTypeInfoProfile] =
            mockUnsafeMethod(name)
        }

        val a1 = "some.class.name"
        val r = mock[ReferenceTypeInfoProfile]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.`class`(a1) should be (r)
      }
    }
  }
}
