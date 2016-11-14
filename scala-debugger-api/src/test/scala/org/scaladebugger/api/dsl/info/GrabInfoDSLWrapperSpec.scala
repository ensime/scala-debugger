package org.scaladebugger.api.dsl.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.profiles.traits.info.{ThreadInfoProfile, GrabInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class GrabInfoDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockGrabInfoProfile = mock[GrabInfoProfile]

  describe("GrabInfoDSLWrapper") {
    describe("#forThread(ThreadReference)") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.GrabInfoDSL

        val threadReference = mock[ThreadReference]
        val returnValue = Success(mock[ThreadInfoProfile])

        (mockGrabInfoProfile.tryThread(_: ThreadReference))
          .expects(threadReference)
          .returning(returnValue).once()

        mockGrabInfoProfile.forThread(threadReference) should be (returnValue)
      }
    }

    describe("#forUnsafeThread(ThreadReference)") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.GrabInfoDSL

        val threadReference = mock[ThreadReference]
        val returnValue = mock[ThreadInfoProfile]

        (mockGrabInfoProfile.thread(_: ThreadReference))
          .expects(threadReference)
          .returning(returnValue).once()

        mockGrabInfoProfile.forUnsafeThread(threadReference) should be (returnValue)
      }
    }

    describe("#forThread(Long)") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.GrabInfoDSL

        val threadId = 999L
        val returnValue = Success(mock[ThreadInfoProfile])

        (mockGrabInfoProfile.tryThread(_: Long))
          .expects(threadId)
          .returning(returnValue).once()

        mockGrabInfoProfile.forThread(threadId) should be (returnValue)
      }
    }

    describe("#forUnsafeThread(Long)") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.GrabInfoDSL

        val threadId = 999L
        val returnValue = mock[ThreadInfoProfile]

        (mockGrabInfoProfile.thread(_: Long))
          .expects(threadId)
          .returning(returnValue).once()

        mockGrabInfoProfile.forUnsafeThread(threadId) should be (returnValue)
      }
    }
  }
}
