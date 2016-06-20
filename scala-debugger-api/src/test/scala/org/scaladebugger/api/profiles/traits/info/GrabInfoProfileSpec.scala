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
    describe("#tryObject(objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(objectReference)
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
      }
    }

    describe("#object(threadInfo, objectReference)") {
      it("should invoke `object`(threadReference, objectReference)") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(
            objectReference
          )
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]

        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
      }
    }

    describe("#tryObject(threadReference, objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfoProfile = mockUnsafeMethod(objectReference)
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
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

    describe("#thread(threadName, threadGroupName)") {
      it("should return the Some result of threadOption(threadName, threadGroupName)") {
        val mockUnsafeMethod = mockFunction[String, String, Option[ThreadInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(
            threadName: String,
            threadGroupName: String
          ): Option[ThreadInfoProfile] = mockUnsafeMethod(threadName, threadGroupName)
        }

        val a1 = "someThreadName"
        val a2 = "someThreadGroupName"
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1, a2).returning(Some(r)).once()
        grabInfoProfile.thread(a1, a2) should be (r)
      }
    }

    describe("#tryThread(threadName, threadGroupName)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, String, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(
            threadName: String,
            threadGroupName: String
          ): ThreadInfoProfile = mockUnsafeMethod(threadName, threadGroupName)
        }

        val a1 = "someThreadName"
        val a2 = "someThreadGroupName"
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        grabInfoProfile.tryThread(a1, a2).get should be (r)
      }
    }

    describe("#threadOption(threadName, threadGroupName)") {
      it("should return Some(profile) if a thread with the name and thread group name is found") {
        val expected = Some(mock[ThreadInfoProfile])
        val threadGroupName = "someThreadGroupName"
        val threadName = "someThreadName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(expected.get)
        }

        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]
        (mockThreadGroupInfo.name _).expects().returning(threadGroupName).once()

        (expected.get.threadGroup _).expects()
          .returning(mockThreadGroupInfo).once()
        (expected.get.name _).expects().returning(threadName).once()

        val actual = grabInfoProfile.threadOption(threadName, threadGroupName)

        actual should be (expected)
      }

      it("should return None if no thread with matching name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfoProfile]
        val threadGroupName = "someThreadGroupName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.threadGroup _).expects().never() // Short circuit
        (mockThreadInfo.name _).expects().returning("someOtherName").once()
        val actual = grabInfoProfile.threadOption("someName", threadGroupName)

        actual should be (expected)
      }

      it("should return None if no thread with matching thread group name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfoProfile]
        val threadName = "someThreadName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(mockThreadInfo)
        }

        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]
        (mockThreadGroupInfo.name _).expects().returning("someOtherName").once()

        (mockThreadInfo.threadGroup _).expects()
          .returning(mockThreadGroupInfo).once()
        (mockThreadInfo.name _).expects().returning(threadName).once()
        val actual = grabInfoProfile.threadOption(threadName, "someName")

        actual should be (expected)
      }
    }

    describe("#thread(name)") {
      it("should return the Some result of threadOption(name)") {
        val mockUnsafeMethod = mockFunction[String, Option[ThreadInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(name: String): Option[ThreadInfoProfile] =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.thread(a1) should be (r)
      }
    }

    describe("#tryThread(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(name: String): ThreadInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#threadOption(name)") {
      it("should return Some(profile) if a thread with matching name is found") {
        val expected = Some(mock[ThreadInfoProfile])
        val threadName = "someName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(expected.get)
        }

        (expected.get.name _).expects().returning(threadName).once()

        val actual = grabInfoProfile.threadOption(threadName)

        actual should be (expected)
      }

      it("should return None if no thread with a matching name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.name _).expects().returning("someOtherName").once()
        val actual = grabInfoProfile.threadOption("someName")

        actual should be (expected)
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

    describe("#threadOption(threadId)") {
      it("should return Some(profile) if a thread with matching unique id is found") {
        val expected = Some(mock[ThreadInfoProfile])
        val threadId = 999L

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(expected.get)
        }

        (expected.get.uniqueId _).expects().returning(threadId).once()

        val actual = grabInfoProfile.threadOption(threadId)

        actual should be (expected)
      }

      it("should return None if no thread with a matching unique id is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfoProfile] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.uniqueId _).expects().returning(998L).once()
        val actual = grabInfoProfile.threadOption(999L)

        actual should be (expected)
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

    describe("#tryThreadGroup(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(name: String): ThreadGroupInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
      }
    }

    describe("#threadGroup(name)") {
      it("should return the Some result of threadGroupOption(threadGroupId)") {
        val mockUnsafeMethod = mockFunction[String, Option[ThreadGroupInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroupOption(
            name: String
          ): Option[ThreadGroupInfoProfile] = mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.threadGroup(a1) should be (r)
      }
    }

    describe("#threadGroupOption(threadGroupId)") {
      it("should return Some(profile) if a threadGroup with matching unique id is found") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val threadGroupId = 999L

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(expected.get)
        }

        (expected.get.uniqueId _).expects().returning(threadGroupId).once()

        val actual = grabInfoProfile.threadGroupOption(threadGroupId)

        actual should be (expected)
      }

      it("should recurse through each thread group's subgroups to find a match") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(mockThreadGroupInfo)
        }

        (mockThreadGroupInfo.uniqueId _).expects().returning(998L).once()

        (mockThreadGroupInfo.threadGroups _).expects()
          .returning(Seq(expected.get)).once()

        (expected.get.uniqueId _).expects().returning(999L).once()

        val actual = grabInfoProfile.threadGroupOption(999L)

        actual should be (expected)
      }

      it("should return None if no threadGroup with a matching unique id is found") {
        val expected = None
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(mockThreadGroupInfo)
        }

        (mockThreadGroupInfo.uniqueId _).expects().returning(998L).once()

        (mockThreadGroupInfo.threadGroups _).expects().returning(Nil).once()

        val actual = grabInfoProfile.threadGroupOption(999L)

        actual should be (expected)
      }
    }

    describe("#threadGroupOption(name)") {
      it("should return Some(profile) if a threadGroup with matching name is found") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val threadGroupName = "someName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(expected.get)
        }

        (expected.get.name _).expects().returning(threadGroupName).once()

        val actual = grabInfoProfile.threadGroupOption(threadGroupName)

        actual should be (expected)
      }

      it("should recurse through each thread group's subgroups to find a match") {
        val expected = Some(mock[ThreadGroupInfoProfile])
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(mockThreadGroupInfo)
        }

        (mockThreadGroupInfo.name _).expects().returning("someOtherName").once()

        (mockThreadGroupInfo.threadGroups _).expects()
          .returning(Seq(expected.get)).once()

        (expected.get.name _).expects().returning("someName").once()

        val actual = grabInfoProfile.threadGroupOption("someName")

        actual should be (expected)
      }

      it("should return None if no threadGroup with a matching name is found") {
        val expected = None
        val mockThreadGroupInfo = mock[ThreadGroupInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfoProfile] =
            Seq(mockThreadGroupInfo)
        }

        (mockThreadGroupInfo.name _).expects().returning("someOtherName").once()

        (mockThreadGroupInfo.threadGroups _).expects().returning(Nil).once()

        val actual = grabInfoProfile.threadGroupOption("someName")

        actual should be (expected)
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

    describe("#classOption(name)") {
      it("should return Some(profile) if a class with matching name is found") {
        val expected = Some(mock[ReferenceTypeInfoProfile])
        val name = "some.class.name"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfoProfile] = Seq(expected.get)
        }

        (expected.get.name _).expects().returning(name).once()

        val actual = grabInfoProfile.classOption(name)

        actual should be (expected)
      }

      it("should return None if no class with a matching name is found") {
        val expected = None
        val name = "some.class.name"

        val mockReferenceTypeInfo = mock[ReferenceTypeInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfoProfile] =
            Seq(mockReferenceTypeInfo)
        }

        (mockReferenceTypeInfo.name _).expects().returning(name + 1).once()

        val actual = grabInfoProfile.classOption(name)

        actual should be (expected)
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
