package org.scaladebugger.api.lowlevel.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestMonitorContendedEnterManager}

import scala.util.{Failure, Success}

class PendingMonitorContendedEnterSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]

  private class TestMonitorContendedEnterInfoPendingActionManager
    extends PendingActionManager[MonitorContendedEnterRequestInfo]
  private val mockPendingActionManager =
    mock[TestMonitorContendedEnterInfoPendingActionManager]

  private val pendingMonitorContendedEnterSupport = new TestMonitorContendedEnterManager(
    mockMonitorContendedEnterManager
  ) with PendingMonitorContendedEnterSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MonitorContendedEnterRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMonitorContendedEnterSupport") {
    describe("#processAllPendingMonitorContendedEnterRequests") {
      it("should process all pending monitor contended enter requests") {
        val expected = Seq(
          MonitorContendedEnterRequestInfo(TestRequestId, true),
          MonitorContendedEnterRequestInfo(TestRequestId + 1, true),
          MonitorContendedEnterRequestInfo(TestRequestId + 2, true)
        )

        // Create monitor contended enter requests to use for testing
        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorContendedEnterSupport.createMonitorContendedEnterRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingMonitorContendedEnterSupport.processAllPendingMonitorContendedEnterRequests()
        actual should be (expected)
      }
    }

    describe("#pendingMonitorContendedEnterRequests") {
      it("should return a collection of pending monitor contended enter requests") {
        val expected = Seq(
          MonitorContendedEnterRequestInfo(TestRequestId, true),
          MonitorContendedEnterRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          MonitorContendedEnterRequestInfo(TestRequestId + 2, true)
        )

        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorContendedEnterSupport.createMonitorContendedEnterRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingMonitorContendedEnterSupport.pendingMonitorContendedEnterRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending monitor contended enter requests") {
        val expected = Nil

        // No pending monitor contended enter requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMonitorContendedEnterSupport.pendingMonitorContendedEnterRequests

        actual should be (expected)
      }
    }

    describe("#createMonitorContendedEnterRequestWithId") {
      it("should return Success(id) if the monitor contended enter was created") {
        val expected = Success(TestRequestId)

        // Create a monitor contended enter to use for testing
        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingMonitorContendedEnterSupport.createMonitorContendedEnterRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending monitor contended enter if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending monitor contended enter should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MonitorContendedEnterRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMonitorContendedEnterSupport.createMonitorContendedEnterRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingMonitorContendedEnterSupport.disablePendingSupport()
        val actual = pendingMonitorContendedEnterSupport.createMonitorContendedEnterRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnterRequest") {
      it("should return true if the monitor contended enter was successfully deleted") {
        val expected = true

        (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending monitor contended enter requests
        // (performed by standard removeMonitorContendedEnterRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorContendedEnterSupport.removeMonitorContendedEnterRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending monitor contended enter request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending monitor contended enter requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            MonitorContendedEnterRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingMonitorContendedEnterSupport.removeMonitorContendedEnterRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the monitor contended enter request was not found") {
        val expected = false

        (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending monitor contended enter requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorContendedEnterSupport.removeMonitorContendedEnterRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

