package org.scaladebugger.api.lowlevel.threads

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestThreadStartManager}

import scala.util.{Failure, Success}

class PendingThreadStartSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadStartManager = mock[ThreadStartManager]

  private class TestThreadStartInfoPendingActionManager
    extends PendingActionManager[ThreadStartRequestInfo]
  private val mockPendingActionManager =
    mock[TestThreadStartInfoPendingActionManager]

  private val pendingThreadStartSupport = new TestThreadStartManager(
    mockThreadStartManager
  ) with PendingThreadStartSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[ThreadStartRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingThreadStartSupport") {
    describe("#processAllPendingThreadStartRequests") {
      it("should process all pending thread start requests") {
        val expected = Seq(
          ThreadStartRequestInfo(TestRequestId, true),
          ThreadStartRequestInfo(TestRequestId + 1, true),
          ThreadStartRequestInfo(TestRequestId + 2, true)
        )

        // Create thread start requests to use for testing
        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingThreadStartSupport.createThreadStartRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingThreadStartSupport.processAllPendingThreadStartRequests()
        actual should be (expected)
      }
    }

    describe("#pendingThreadStartRequests") {
      it("should return a collection of pending thread start requests") {
        val expected = Seq(
          ThreadStartRequestInfo(TestRequestId, true),
          ThreadStartRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          ThreadStartRequestInfo(TestRequestId + 2, true)
        )

        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingThreadStartSupport.createThreadStartRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingThreadStartSupport.pendingThreadStartRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending thread start requests") {
        val expected = Nil

        // No pending thread start requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingThreadStartSupport.pendingThreadStartRequests

        actual should be (expected)
      }
    }

    describe("#createThreadStartRequestWithId") {
      it("should return Success(id) if the thread start was created") {
        val expected = Success(TestRequestId)

        // Create a thread start to use for testing
        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingThreadStartSupport.createThreadStartRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending thread start if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending thread start should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          ThreadStartRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingThreadStartSupport.createThreadStartRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingThreadStartSupport.disablePendingSupport()
        val actual = pendingThreadStartSupport.createThreadStartRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeThreadStartRequest") {
      it("should return true if the thread start was successfully deleted") {
        val expected = true

        (mockThreadStartManager.removeThreadStartRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending thread start requests
        // (performed by standard removeThreadStartRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingThreadStartSupport.removeThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending thread start request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending thread start requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            ThreadStartRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockThreadStartManager.removeThreadStartRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingThreadStartSupport.removeThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the thread start request was not found") {
        val expected = false

        (mockThreadStartManager.removeThreadStartRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending thread start requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingThreadStartSupport.removeThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

