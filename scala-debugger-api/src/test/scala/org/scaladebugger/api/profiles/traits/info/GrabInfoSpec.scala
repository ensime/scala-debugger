package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.{ObjectReference, ThreadGroupReference, ThreadReference}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestGrabInfoProfile

import scala.util.Success

class GrabInfoSpec extends test.ParallelMockFunSpec
{
  describe("GrabInfoProfile") {
    describe("#tryObject(objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfo = mockUnsafeMethod(objectReference)
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
      }
    }

    describe("#object(threadInfo, objectReference)") {
      it("should invoke `object`(threadReference, objectReference)") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfo = mockUnsafeMethod(
            objectReference
          )
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfo]

        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
      }
    }

    describe("#tryObject(threadReference, objectReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectReference, ObjectInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `object`(
            objectReference: ObjectReference
          ): ObjectInfo = mockUnsafeMethod(objectReference)
        }

        val a1 = mock[ObjectReference]
        val r = mock[ObjectInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryObject(a1).get should be (r)
      }
    }

    describe("#tryThreads") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ThreadInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ThreadInfo])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryThreads.get should be (r)
      }
    }

    describe("#thread(threadName, threadGroupName)") {
      it("should return the Some result of threadOption(threadName, threadGroupName)") {
        val mockUnsafeMethod = mockFunction[String, String, Option[ThreadInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(
            threadName: String,
            threadGroupName: String
          ): Option[ThreadInfo] = mockUnsafeMethod(threadName, threadGroupName)
        }

        val a1 = "someThreadName"
        val a2 = "someThreadGroupName"
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1, a2).returning(Some(r)).once()
        grabInfoProfile.thread(a1, a2) should be (r)
      }
    }

    describe("#tryThread(threadName, threadGroupName)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, String, ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(
            threadName: String,
            threadGroupName: String
          ): ThreadInfo = mockUnsafeMethod(threadName, threadGroupName)
        }

        val a1 = "someThreadName"
        val a2 = "someThreadGroupName"
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        grabInfoProfile.tryThread(a1, a2).get should be (r)
      }
    }

    describe("#threadOption(threadName, threadGroupName)") {
      it("should return Some(profile) if a thread with the name and thread group name is found") {
        val expected = Some(mock[ThreadInfo])
        val threadGroupName = "someThreadGroupName"
        val threadName = "someThreadName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(expected.get)
        }

        val mockThreadGroupInfo = mock[ThreadGroupInfo]
        (mockThreadGroupInfo.name _).expects().returning(threadGroupName).once()

        (expected.get.threadGroup _).expects()
          .returning(mockThreadGroupInfo).once()
        (expected.get.name _).expects().returning(threadName).once()

        val actual = grabInfoProfile.threadOption(threadName, threadGroupName)

        actual should be (expected)
      }

      it("should return None if no thread with matching name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfo]
        val threadGroupName = "someThreadGroupName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.threadGroup _).expects().never() // Short circuit
        (mockThreadInfo.name _).expects().returning("someOtherName").once()
        val actual = grabInfoProfile.threadOption("someName", threadGroupName)

        actual should be (expected)
      }

      it("should return None if no thread with matching thread group name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfo]
        val threadName = "someThreadName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(mockThreadInfo)
        }

        val mockThreadGroupInfo = mock[ThreadGroupInfo]
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
        val mockUnsafeMethod = mockFunction[String, Option[ThreadInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(name: String): Option[ThreadInfo] =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.thread(a1) should be (r)
      }
    }

    describe("#tryThread(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(name: String): ThreadInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#threadOption(name)") {
      it("should return Some(profile) if a thread with matching name is found") {
        val expected = Some(mock[ThreadInfo])
        val threadName = "someName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(expected.get)
        }

        (expected.get.name _).expects().returning(threadName).once()

        val actual = grabInfoProfile.threadOption(threadName)

        actual should be (expected)
      }

      it("should return None if no thread with a matching name is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.name _).expects().returning("someOtherName").once()
        val actual = grabInfoProfile.threadOption("someName")

        actual should be (expected)
      }
    }

    describe("#tryThread(threadId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadId: Long): ThreadInfo =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#thread(threadId)") {
      it("should return the Some result of threadOption(threadId)") {
        val mockUnsafeMethod = mockFunction[Long, Option[ThreadInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadOption(threadId: Long): Option[ThreadInfo] =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.thread(a1) should be (r)
      }
    }

    describe("#threadOption(threadId)") {
      it("should return Some(profile) if a thread with matching unique id is found") {
        val expected = Some(mock[ThreadInfo])
        val threadId = 999L

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(expected.get)
        }

        (expected.get.uniqueId _).expects().returning(threadId).once()

        val actual = grabInfoProfile.threadOption(threadId)

        actual should be (expected)
      }

      it("should return None if no thread with a matching unique id is found") {
        val expected = None
        val mockThreadInfo = mock[ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threads: Seq[ThreadInfo] = Seq(mockThreadInfo)
        }

        (mockThreadInfo.uniqueId _).expects().returning(998L).once()
        val actual = grabInfoProfile.threadOption(999L)

        actual should be (expected)
      }
    }

    describe("#tryThread(threadReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ThreadInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadReference: ThreadReference): ThreadInfo =
            mockUnsafeMethod(threadReference)
        }

        val a1 = mock[ThreadReference]
        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#tryThreadGroups") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ThreadGroupInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ThreadGroupInfo])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryThreadGroups.get should be (r)
      }
    }

    describe("#tryThreadGroup(threadGroupId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(threadGroupId: Long): ThreadGroupInfo =
            mockUnsafeMethod(threadGroupId)
        }

        val a1 = 999L
        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
      }
    }

    describe("#threadGroup(threadGroupId)") {
      it("should return the Some result of threadGroupOption(threadGroupId)") {
        val mockUnsafeMethod = mockFunction[Long, Option[ThreadGroupInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroupOption(
            threadGroupId: Long
          ): Option[ThreadGroupInfo] = mockUnsafeMethod(threadGroupId)
        }

        val a1 = 999L
        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.threadGroup(a1) should be (r)
      }
    }

    describe("#tryThreadGroup(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(name: String): ThreadGroupInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
      }
    }

    describe("#threadGroup(name)") {
      it("should return the Some result of threadGroupOption(threadGroupId)") {
        val mockUnsafeMethod = mockFunction[String, Option[ThreadGroupInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroupOption(
            name: String
          ): Option[ThreadGroupInfo] = mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.threadGroup(a1) should be (r)
      }
    }

    describe("#threadGroupOption(threadGroupId)") {
      it("should return Some(profile) if a threadGroup with matching unique id is found") {
        val expected = Some(mock[ThreadGroupInfo])
        val threadGroupId = 999L

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
            Seq(expected.get)
        }

        (expected.get.uniqueId _).expects().returning(threadGroupId).once()

        val actual = grabInfoProfile.threadGroupOption(threadGroupId)

        actual should be (expected)
      }

      it("should recurse through each thread group's subgroups to find a match") {
        val expected = Some(mock[ThreadGroupInfo])
        val mockThreadGroupInfo = mock[ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
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
        val mockThreadGroupInfo = mock[ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
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
        val expected = Some(mock[ThreadGroupInfo])
        val threadGroupName = "someName"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
            Seq(expected.get)
        }

        (expected.get.name _).expects().returning(threadGroupName).once()

        val actual = grabInfoProfile.threadGroupOption(threadGroupName)

        actual should be (expected)
      }

      it("should recurse through each thread group's subgroups to find a match") {
        val expected = Some(mock[ThreadGroupInfo])
        val mockThreadGroupInfo = mock[ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
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
        val mockThreadGroupInfo = mock[ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroups: Seq[ThreadGroupInfo] =
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
        val mockUnsafeMethod = mockFunction[ThreadGroupReference, ThreadGroupInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def threadGroup(
            threadGroupReference: ThreadGroupReference
          ): ThreadGroupInfo = mockUnsafeMethod(threadGroupReference)
        }

        val a1 = mock[ThreadGroupReference]
        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThreadGroup(a1).get should be (r)
      }
    }

    describe("#tryClasses") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ReferenceTypeInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ReferenceTypeInfo])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryClasses.get should be (r)
      }
    }

    describe("#tryClass(name)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ReferenceTypeInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def `class`(name: String): ReferenceTypeInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "some.class.name"
        val r = mock[ReferenceTypeInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryClass(a1).get should be (r)
      }
    }

    describe("#classOption(name)") {
      it("should return Some(profile) if a class with matching name is found") {
        val expected = Some(mock[ReferenceTypeInfo])
        val name = "some.class.name"

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfo] = Seq(expected.get)
        }

        (expected.get.name _).expects().returning(name).once()

        val actual = grabInfoProfile.classOption(name)

        actual should be (expected)
      }

      it("should return None if no class with a matching name is found") {
        val expected = None
        val name = "some.class.name"

        val mockReferenceTypeInfo = mock[ReferenceTypeInfo]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfo] =
            Seq(mockReferenceTypeInfo)
        }

        (mockReferenceTypeInfo.name _).expects().returning(name + 1).once()

        val actual = grabInfoProfile.classOption(name)

        actual should be (expected)
      }
    }

    describe("#`class`(name)") {
      it("should return the Some result of classOption(name)") {
        val mockUnsafeMethod = mockFunction[String, Option[ReferenceTypeInfo]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classOption(name: String): Option[ReferenceTypeInfo] =
            mockUnsafeMethod(name)
        }

        val a1 = "some.class.name"
        val r = mock[ReferenceTypeInfo]
        mockUnsafeMethod.expects(a1).returning(Some(r)).once()
        grabInfoProfile.`class`(a1) should be (r)
      }
    }
  }
}
