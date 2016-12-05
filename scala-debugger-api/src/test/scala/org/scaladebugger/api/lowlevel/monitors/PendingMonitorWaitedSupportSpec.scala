package org.scaladebugger.api.lowlevel.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestMonitorWaitedManager}

import scala.util.{Failure, Success}

class PendingMonitorWaitedSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorWaitedManager = mock[MonitorWaitedManager]

  private class TestMonitorWaitedInfoPendingActionManager
    extends PendingActionManager[MonitorWaitedRequestInfo]
  private val mockPendingActionManager =
    mock[TestMonitorWaitedInfoPendingActionManager]

  private val pendingMonitorWaitedSupport = new TestMonitorWaitedManager(
    mockMonitorWaitedManager
  ) with PendingMonitorWaitedSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MonitorWaitedRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMonitorWaitedSupport") {
    describe("#processAllPendingMonitorWaitedRequests") {
      it("should process all pending monitor waited requests") {
        val expected = Seq(
          MonitorWaitedRequestInfo(TestRequestId, true),
          MonitorWaitedRequestInfo(TestRequestId + 1, true),
          MonitorWaitedRequestInfo(TestRequestId + 2, true)
        )

        // Create monitor waited requests to use for testing
        (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorWaitedSupport.createMonitorWaitedRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingMonitorWaitedSupport.processAllPendingMonitorWaitedRequests()
        actual should be (expected)
      }
    }

    describe("#pendingMonitorWaitedRequests") {
      it("should return a collection of pending monitor waited requests") {
        val expected = Seq(
          MonitorWaitedRequestInfo(TestRequestId, true),
          MonitorWaitedRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          MonitorWaitedRequestInfo(TestRequestId + 2, true)
        )

        (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorWaitedSupport.createMonitorWaitedRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingMonitorWaitedSupport.pendingMonitorWaitedRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending monitor waited requests") {
        val expected = Nil

        // No pending monitor waited requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMonitorWaitedSupport.pendingMonitorWaitedRequests

        actual should be (expected)
      }
    }

    describe("#createMonitorWaitedRequestWithId") {
      it("should return Success(id) if the monitor waited was created") {
        val expected = Success(TestRequestId)

        // Create a monitor waited to use for testing
        (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingMonitorWaitedSupport.createMonitorWaitedRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending monitor waited if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending monitor waited should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MonitorWaitedRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMonitorWaitedSupport.createMonitorWaitedRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingMonitorWaitedSupport.disablePendingSupport()
        val actual = pendingMonitorWaitedSupport.createMonitorWaitedRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitedRequest") {
      it("should return true if the monitor waited was successfully deleted") {
        val expected = true

        (mockMonitorWaitedManager.removeMonitorWaitedRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending monitor waited requests
        // (performed by standard removeMonitorWaitedRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorWaitedSupport.removeMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending monitor waited request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending monitor waited requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            MonitorWaitedRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingMonitorWaitedSupport.removeMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the monitor waited request was not found") {
        val expected = false

        (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending monitor waited requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorWaitedSupport.removeMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

