package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses
import test.InfoTestClasses.TestThreadInfo

import scala.util.{Success, Failure, Try}

class ThreadInfoSpec extends test.ParallelMockFunSpec
{
  describe("ThreadInfo") {
    describe("#suspendAndExecute") {
      it("should suspend the thread, execute the code, and then resume the thread") {
        val mockSuspend = mockFunction[Unit]
        val mockResume = mockFunction[Unit]

        val threadInfoProfile = new TestThreadInfo {
          override def suspend(): Unit = mockSuspend()
          override def resume(): Unit = mockResume()
        }

        inSequence {
          mockSuspend.expects().once()
          mockResume.expects().once()
        }

        val result = threadInfoProfile.suspendAndExecute(throw new Throwable)

        result shouldBe a [Failure[_]]
      }
    }

    describe("#findVariableByName") {
      it("should return None if no variable is found") {
        val expected = None
        val name = "someName"

        val threadInfoProfile = new TestThreadInfo {
          // Return 0 frames to test no variable found
          override def totalFrames: Int = 0
        }
        val actual = threadInfoProfile.findVariableByName(name)

        actual should be (expected)
      }

      it("should return Some(variable) if found") {
        val expected = Some(mock[VariableInfo])
        val name = "someName"

        val mockFrame = mock[FrameInfo]
        (mockFrame.tryIndexedVariable _).expects(name)
          .returning(Success(expected.get)).once()

        val threadInfoProfile = new TestThreadInfo {
          override def frame(index: Int): FrameInfo = mockFrame
          override def totalFrames: Int = 1
        }
        val actual = threadInfoProfile.findVariableByName(name)

        actual should be (expected)
      }
    }

    describe("#tryFindVariableByName") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, Option[VariableInfo]]

        val threadInfoProfile = new TestThreadInfo {
          override def findVariableByName(
            name: String
          ): Option[VariableInfo] = mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = Some(mock[VariableInfo])
        mockUnsafeMethod.expects(a1).returning(r).once()
        threadInfoProfile.tryFindVariableByName(a1).get should be (r.get)
      }
    }

    describe("#findVariableByIndex") {
      it("should return None if no variable is found") {
        val expected = None
        val frameIndex = 1
        val offsetIndex = 2

        val mockFrame = mock[FrameInfo]
        (mockFrame.indexedLocalVariables _).expects()
          .returning(Nil).once()

        val threadInfoProfile = new TestThreadInfo {
          override def frame(index: Int): FrameInfo = {
            require(index == frameIndex)
            mockFrame
          }
        }
        val actual = threadInfoProfile.findVariableByIndex(
          frameIndex,
          offsetIndex
        )

        actual should be (expected)
      }

      it("should return Some(variable) if found") {
        val expected = Some(mock[IndexedVariableInfo])
        val frameIndex = 1
        val offsetIndex = 2

        val mockFrame = mock[FrameInfo]
        (mockFrame.indexedLocalVariables _).expects()
          .returning(Seq(expected.get)).once()

        (expected.get.offsetIndex _).expects().returning(offsetIndex).once()

        val threadInfoProfile = new TestThreadInfo {
          override def frame(index: Int): FrameInfo = {
            require(index == frameIndex)
            mockFrame
          }
        }
        val actual = threadInfoProfile.findVariableByIndex(
          frameIndex,
          offsetIndex
        )

        actual should be (expected)
      }
    }

    describe("#tryFindVariableByIndex") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Int, Option[VariableInfo]]

        val threadInfoProfile = new TestThreadInfo {
          override def findVariableByIndex(
            frameIndex: Int, offsetIndex: Int
          ): Option[VariableInfo] = mockUnsafeMethod(frameIndex, offsetIndex)
        }

        val a1 = 1
        val a2 = 2
        val r = Some(mock[VariableInfo])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        threadInfoProfile.tryFindVariableByIndex(a1, a2).get should be (r.get)
      }
    }

    describe("#toPrettyString") {
      it("should display the thread name and unique id as a hex code") {
        val expected = "Thread threadName (0xABCDE)"

        val threadInfoProfile = new TestThreadInfo {
          override def uniqueId: Long = Integer.parseInt("ABCDE", 16)
          override def name: String = "threadName"
        }

        val actual = threadInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryFrames()") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FrameInfo]]

        val threadInfoProfile = new TestThreadInfo {
          override def frames: Seq[FrameInfo] = mockUnsafeMethod()
        }

        val r = Seq(mock[FrameInfo])
        mockUnsafeMethod.expects().returning(r).once()
        threadInfoProfile.tryFrames.get should be (r)
      }
    }

    describe("#frames(index, length)") {
      it("should cap the total requested frames if the length is too long") {
        val expected = 3
        val index = 1
        val length = 4 // One too many
        val frameCount = 4

        val mockRawFrameFunction = mockFunction[Int, Int, Seq[FrameInfo]]
        val threadInfoProfile = new TestThreadInfo {
          override def totalFrames: Int = frameCount
          override def rawFrames(index: Int, length: Int): Seq[FrameInfo] =
            mockRawFrameFunction(index, length)
        }

        mockRawFrameFunction.expects(index, expected).returning(Nil).once()

        threadInfoProfile.frames(index, length)
      }

      it("should cap the total requested frames if the length is less than zero") {
        val expected = 3
        val index = 1
        val length = -1 // Requesting all frames including and after index
        val frameCount = 4

        val mockRawFrameFunction = mockFunction[Int, Int, Seq[FrameInfo]]
        val threadInfoProfile = new TestThreadInfo {
          override def totalFrames: Int = frameCount
          override def rawFrames(index: Int, length: Int): Seq[FrameInfo] =
            mockRawFrameFunction(index, length)
        }

        mockRawFrameFunction.expects(index, expected).returning(Nil).once()

        threadInfoProfile.frames(index, length)
      }

      it("should use the normal length ") {
        val expected = 2
        val index = 1
        val length = 2
        val frameCount = 4

        val mockRawFrameFunction = mockFunction[Int, Int, Seq[FrameInfo]]
        val threadInfoProfile = new TestThreadInfo {
          override def totalFrames: Int = frameCount
          override def rawFrames(index: Int, length: Int): Seq[FrameInfo] =
            mockRawFrameFunction(index, length)
        }

        mockRawFrameFunction.expects(index, expected).returning(Nil).once()

        threadInfoProfile.frames(index, length)
      }
    }

    describe("#tryFrames(index, length)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Int, Seq[FrameInfo]]

        val threadInfoProfile = new TestThreadInfo {
          override def frames(index: Int, length: Int): Seq[FrameInfo] =
            mockUnsafeMethod(index, length)
        }

        val a1 = 99
        val a2 = 100
        val r = Seq(mock[FrameInfo])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        threadInfoProfile.tryFrames(a1, a2).get should be (r)
      }
    }

    describe("#tryFrame") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, FrameInfo]

        val threadInfoProfile = new TestThreadInfo {
          override def frame(index: Int): FrameInfo =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[FrameInfo]

        mockUnsafeMethod.expects(a1).returning(r).once()
        threadInfoProfile.tryFrame(a1).get should be (r)
      }
    }

    describe("#tryTopFrame") {
      it("should invoke withFrame(0) underneath") {
        val mockUnsafeMethod = mockFunction[Int, Try[FrameInfo]]

        val threadInfoProfile = new TestThreadInfo {
          override def tryFrame(index: Int): Try[FrameInfo] =
            mockUnsafeMethod(index)
        }

        val r = Success(mock[FrameInfo])

        mockUnsafeMethod.expects(0).returning(r).once()
        threadInfoProfile.tryTopFrame should be (r)
      }
    }

    describe("#topFrame") {
      it("should invoke withUnsafeFrame(0) underneath") {
        val mockUnsafeMethod = mockFunction[Int, FrameInfo]

        val threadInfoProfile = new TestThreadInfo {
          override def frame(index: Int): FrameInfo =
            mockUnsafeMethod(index)
        }

        val r = mock[FrameInfo]

        mockUnsafeMethod.expects(0).returning(r).once()
        threadInfoProfile.topFrame should be (r)
      }
    }
  }
}
