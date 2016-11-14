package org.scaladebugger.api.lowlevel.monitors
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestMonitorWaitManager}

import scala.util.{Failure, Success}

class PendingMonitorWaitSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorWaitManager = mock[MonitorWaitManager]

  private class TestMonitorWaitInfoPendingActionManager
    extends PendingActionManager[MonitorWaitRequestInfo]
  private val mockPendingActionManager =
    mock[TestMonitorWaitInfoPendingActionManager]

  private val pendingMonitorWaitSupport = new TestMonitorWaitManager(
    mockMonitorWaitManager
  ) with PendingMonitorWaitSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MonitorWaitRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMonitorWaitSupport") {
    describe("#processAllPendingMonitorWaitRequests") {
      it("should process all pending monitor wait requests") {
        val expected = Seq(
          MonitorWaitRequestInfo(TestRequestId, true),
          MonitorWaitRequestInfo(TestRequestId + 1, true),
          MonitorWaitRequestInfo(TestRequestId + 2, true)
        )

        // Create monitor wait requests to use for testing
        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorWaitSupport.createMonitorWaitRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingMonitorWaitSupport.processAllPendingMonitorWaitRequests()
        actual should be (expected)
      }
    }

    describe("#pendingMonitorWaitRequests") {
      it("should return a collection of pending monitor wait requests") {
        val expected = Seq(
          MonitorWaitRequestInfo(TestRequestId, true),
          MonitorWaitRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          MonitorWaitRequestInfo(TestRequestId + 2, true)
        )

        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorWaitSupport.createMonitorWaitRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingMonitorWaitSupport.pendingMonitorWaitRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending monitor wait requests") {
        val expected = Nil

        // No pending monitor wait requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMonitorWaitSupport.pendingMonitorWaitRequests

        actual should be (expected)
      }
    }

    describe("#createMonitorWaitRequestWithId") {
      it("should return Success(id) if the monitor wait was created") {
        val expected = Success(TestRequestId)

        // Create a monitor wait to use for testing
        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingMonitorWaitSupport.createMonitorWaitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending monitor wait if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending monitor wait should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MonitorWaitRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMonitorWaitSupport.createMonitorWaitRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingMonitorWaitSupport.disablePendingSupport()
        val actual = pendingMonitorWaitSupport.createMonitorWaitRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitRequest") {
      it("should return true if the monitor wait was successfully deleted") {
        val expected = true

        (mockMonitorWaitManager.removeMonitorWaitRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending monitor wait requests
        // (performed by standard removeMonitorWaitRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorWaitSupport.removeMonitorWaitRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending monitor wait request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending monitor wait requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            MonitorWaitRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockMonitorWaitManager.removeMonitorWaitRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingMonitorWaitSupport.removeMonitorWaitRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the monitor wait request was not found") {
        val expected = false

        (mockMonitorWaitManager.removeMonitorWaitRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending monitor wait requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorWaitSupport.removeMonitorWaitRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

