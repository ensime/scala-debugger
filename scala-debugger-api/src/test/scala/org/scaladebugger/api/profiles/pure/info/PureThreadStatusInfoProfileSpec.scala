package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{ReferenceType, StackFrame, ThreadReference, VirtualMachine}
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, ThreadStatusInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureThreadStatusInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockThreadReference = mock[ThreadReference]
  private val pureThreadStatusInfoProfile =
    new PureThreadStatusInfoProfile(mockThreadReference)

  describe("PureThreadStatusInfoProfile") {
    describe("#statusCode") {
      it("should return the status code of the underlying thread") {
        val expected = 999

        (mockThreadReference.status _).expects().returning(expected).once()
        val actual = pureThreadStatusInfoProfile.statusCode

        actual should be (expected)
      }
    }

    describe("#isUnknown") {
      it("should return true if the status of the thread reference is unknown") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_UNKNOWN)

        pureThreadStatusInfoProfile.isUnknown should be (true)
      }

      it("should return false if the status of the thread reference is not unknown") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_UNKNOWN)

        pureThreadStatusInfoProfile.isUnknown should be (false)
      }
    }

    describe("#isZombie") {
      it("should return true if the thread is a zombie") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_ZOMBIE)

        pureThreadStatusInfoProfile.isZombie should be (true)
      }

      it("should return false if the thread is not a zombie") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_ZOMBIE)

        pureThreadStatusInfoProfile.isZombie should be (false)
      }
    }

    describe("#isRunning") {
      it("should return true if the thread is running") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_RUNNING)

        pureThreadStatusInfoProfile.isRunning should be (true)
      }

      it("should return false if the thread is not running") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_RUNNING)

        pureThreadStatusInfoProfile.isRunning should be (false)
      }
    }

    describe("#isSleeping") {
      it("should return true if the thread is sleeping") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_SLEEPING)

        pureThreadStatusInfoProfile.isSleeping should be (true)
      }

      it("should return false if the thread is not sleeping") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_SLEEPING)

        pureThreadStatusInfoProfile.isSleeping should be (false)
      }
    }

    describe("#isMonitor") {
      it("should return true if the thread is monitoring") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_MONITOR)

        pureThreadStatusInfoProfile.isMonitor should be (true)
      }

      it("should return false if the thread is not monitoring") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_MONITOR)

        pureThreadStatusInfoProfile.isMonitor should be (false)
      }
    }

    describe("#isWait") {
      it("should return true if the thread is waiting") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_WAIT)

        pureThreadStatusInfoProfile.isWait should be (true)
      }

      it("should return false if the thread is not waiting") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_WAIT)

        pureThreadStatusInfoProfile.isWait should be (false)
      }
    }

    describe("#isNotStarted") {
      it("should return true if the thread has not been started") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_NOT_STARTED)

        pureThreadStatusInfoProfile.isNotStarted should be (true)
      }

      it("should return false if the thread has been started") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_NOT_STARTED)

        pureThreadStatusInfoProfile.isNotStarted should be (false)
      }
    }

    describe("#isAtBreakpoint") {
      it("should return true if the thread is suspended at a breakpoint") {
        val expected = true

        (mockThreadReference.isAtBreakpoint _).expects()
          .returning(expected).once()

        val actual = pureThreadStatusInfoProfile.isAtBreakpoint

        actual should be (expected)
      }

      it("should return false if the thread is not suspended at a breakpoint") {
        val expected = false

        (mockThreadReference.isAtBreakpoint _).expects()
          .returning(expected).once()

        val actual = pureThreadStatusInfoProfile.isAtBreakpoint

        actual should be (expected)
      }
    }

    describe("#isSuspended") {
      it("should return true if the thread is suspended") {
        val expected = true

        (mockThreadReference.isSuspended _).expects()
          .returning(expected).once()

        val actual = pureThreadStatusInfoProfile.isSuspended

        actual should be (expected)
      }

      it("should return false if the thread is not suspended") {
        val expected = false

        (mockThreadReference.isSuspended _).expects()
          .returning(expected).once()

        val actual = pureThreadStatusInfoProfile.isSuspended

        actual should be (expected)
      }
    }

    describe("#suspendCount") {
      it("should return the suspend count of the underlying thread") {
        val expected = 999

        (mockThreadReference.suspendCount _).expects()
          .returning(expected).once()

        val actual = pureThreadStatusInfoProfile.suspendCount

        actual should be (expected)
      }
    }
  }
}
