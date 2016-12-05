package org.scaladebugger.api.lowlevel.wrappers

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FunSpec}

class ThreadReferenceWrapperSpec extends FunSpec with Matchers
  with MockFactory
{
  describe("ThreadReferenceWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new ThreadReferenceWrapper(null)
        }
      }
    }

    describe("#isUnknown") {
      it("should return true if the status of the thread reference is unknown") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_UNKNOWN)

        new ThreadReferenceWrapper(threadReference).isUnknown should be (true)
      }

      it("should return false if the status of the thread reference is not unknown") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_UNKNOWN)

        new ThreadReferenceWrapper(threadReference).isUnknown should be (false)
      }
    }

    describe("#isZombie") {
      it("should return true if the thread is a zombie") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_ZOMBIE)

        new ThreadReferenceWrapper(threadReference).isZombie should be (true)
      }

      it("should return false if the thread is not a zombie") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_ZOMBIE)

        new ThreadReferenceWrapper(threadReference).isZombie should be (false)
      }
    }

    describe("#isRunning") {
      it("should return true if the thread is running") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_RUNNING)

        new ThreadReferenceWrapper(threadReference).isRunning should be (true)
      }

      it("should return false if the thread is not running") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_RUNNING)

        new ThreadReferenceWrapper(threadReference).isRunning should be (false)
      }
    }

    describe("#isSleeping") {
      it("should return true if the thread is sleeping") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_SLEEPING)

        new ThreadReferenceWrapper(threadReference).isSleeping should be (true)
      }

      it("should return false if the thread is not sleeping") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_SLEEPING)

        new ThreadReferenceWrapper(threadReference).isSleeping should be (false)
      }
    }

    describe("#isMonitor") {
      it("should return true if the thread is monitoring") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_MONITOR)

        new ThreadReferenceWrapper(threadReference).isMonitor should be (true)
      }

      it("should return false if the thread is not monitoring") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_MONITOR)

        new ThreadReferenceWrapper(threadReference).isMonitor should be (false)
      }
    }

    describe("#isWait") {
      it("should return true if the thread is waiting") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_WAIT)

        new ThreadReferenceWrapper(threadReference).isWait should be (true)
      }

      it("should return false if the thread is not waiting") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_WAIT)

        new ThreadReferenceWrapper(threadReference).isWait should be (false)
      }
    }

    describe("#isNotStarted") {
      it("should return true if the thread has not been started") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(ThreadReference.THREAD_STATUS_NOT_STARTED)

        new ThreadReferenceWrapper(threadReference).isNotStarted should
          be (true)
      }

      it("should return false if the thread has been started") {
        val threadReference = stub[ThreadReference]
        (threadReference.status _).when()
          .returns(~ThreadReference.THREAD_STATUS_NOT_STARTED)

        new ThreadReferenceWrapper(threadReference).isNotStarted should
          be (false)
      }
    }
  }
}
