package org.scaladebugger.api.profiles.java.info

import com.sun.jdi.{ReferenceType, StackFrame, ThreadReference, VirtualMachine}
import org.scaladebugger.api.profiles.traits.info.{FrameInfo, ThreadStatusInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaThreadStatusInfoSpec extends ParallelMockFunSpec
{
  private val mockThreadReference = mock[ThreadReference]
  private val javaThreadStatusInfoProfile =
    new JavaThreadStatusInfo(mockThreadReference)

  describe("JavaThreadStatusInfo") {
    describe("#statusCode") {
      it("should return the status code of the underlying thread") {
        val expected = 999

        (mockThreadReference.status _).expects().returning(expected).once()
        val actual = javaThreadStatusInfoProfile.statusCode

        actual should be (expected)
      }
    }

    describe("#isUnknown") {
      it("should return true if the status of the thread reference is unknown") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_UNKNOWN)

        javaThreadStatusInfoProfile.isUnknown should be (true)
      }

      it("should return false if the status of the thread reference is not unknown") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_UNKNOWN)

        javaThreadStatusInfoProfile.isUnknown should be (false)
      }
    }

    describe("#isZombie") {
      it("should return true if the thread is a zombie") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_ZOMBIE)

        javaThreadStatusInfoProfile.isZombie should be (true)
      }

      it("should return false if the thread is not a zombie") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_ZOMBIE)

        javaThreadStatusInfoProfile.isZombie should be (false)
      }
    }

    describe("#isRunning") {
      it("should return true if the thread is running") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_RUNNING)

        javaThreadStatusInfoProfile.isRunning should be (true)
      }

      it("should return false if the thread is not running") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_RUNNING)

        javaThreadStatusInfoProfile.isRunning should be (false)
      }
    }

    describe("#isSleeping") {
      it("should return true if the thread is sleeping") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_SLEEPING)

        javaThreadStatusInfoProfile.isSleeping should be (true)
      }

      it("should return false if the thread is not sleeping") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_SLEEPING)

        javaThreadStatusInfoProfile.isSleeping should be (false)
      }
    }

    describe("#isMonitor") {
      it("should return true if the thread is monitoring") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_MONITOR)

        javaThreadStatusInfoProfile.isMonitor should be (true)
      }

      it("should return false if the thread is not monitoring") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_MONITOR)

        javaThreadStatusInfoProfile.isMonitor should be (false)
      }
    }

    describe("#isWait") {
      it("should return true if the thread is waiting") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_WAIT)

        javaThreadStatusInfoProfile.isWait should be (true)
      }

      it("should return false if the thread is not waiting") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_WAIT)

        javaThreadStatusInfoProfile.isWait should be (false)
      }
    }

    describe("#isNotStarted") {
      it("should return true if the thread has not been started") {
        (mockThreadReference.status _).expects()
          .returning(ThreadReference.THREAD_STATUS_NOT_STARTED)

        javaThreadStatusInfoProfile.isNotStarted should be (true)
      }

      it("should return false if the thread has been started") {
        (mockThreadReference.status _).expects()
          .returning(~ThreadReference.THREAD_STATUS_NOT_STARTED)

        javaThreadStatusInfoProfile.isNotStarted should be (false)
      }
    }

    describe("#isAtBreakpoint") {
      it("should return true if the thread is suspended at a breakpoint") {
        val expected = true

        (mockThreadReference.isAtBreakpoint _).expects()
          .returning(expected).once()

        val actual = javaThreadStatusInfoProfile.isAtBreakpoint

        actual should be (expected)
      }

      it("should return false if the thread is not suspended at a breakpoint") {
        val expected = false

        (mockThreadReference.isAtBreakpoint _).expects()
          .returning(expected).once()

        val actual = javaThreadStatusInfoProfile.isAtBreakpoint

        actual should be (expected)
      }
    }

    describe("#isSuspended") {
      it("should return true if the thread is suspended") {
        val expected = true

        (mockThreadReference.isSuspended _).expects()
          .returning(expected).once()

        val actual = javaThreadStatusInfoProfile.isSuspended

        actual should be (expected)
      }

      it("should return false if the thread is not suspended") {
        val expected = false

        (mockThreadReference.isSuspended _).expects()
          .returning(expected).once()

        val actual = javaThreadStatusInfoProfile.isSuspended

        actual should be (expected)
      }
    }

    describe("#suspendCount") {
      it("should return the suspend count of the underlying thread") {
        val expected = 999

        (mockThreadReference.suspendCount _).expects()
          .returning(expected).once()

        val actual = javaThreadStatusInfoProfile.suspendCount

        actual should be (expected)
      }
    }
  }
}
