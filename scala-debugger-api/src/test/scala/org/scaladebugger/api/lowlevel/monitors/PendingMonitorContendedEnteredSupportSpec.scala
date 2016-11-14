package org.scaladebugger.api.lowlevel.monitors
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestMonitorContendedEnteredManager}

import scala.util.{Failure, Success}

class PendingMonitorContendedEnteredSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnteredManager = mock[MonitorContendedEnteredManager]

  private class TestMonitorContendedEnteredInfoPendingActionManager
    extends PendingActionManager[MonitorContendedEnteredRequestInfo]
  private val mockPendingActionManager =
    mock[TestMonitorContendedEnteredInfoPendingActionManager]

  private val pendingMonitorContendedEnteredSupport = new TestMonitorContendedEnteredManager(
    mockMonitorContendedEnteredManager
  ) with PendingMonitorContendedEnteredSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MonitorContendedEnteredRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMonitorContendedEnteredSupport") {
    describe("#processAllPendingMonitorContendedEnteredRequests") {
      it("should process all pending monitor contended entered requests") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, true),
          MonitorContendedEnteredRequestInfo(TestRequestId + 1, true),
          MonitorContendedEnteredRequestInfo(TestRequestId + 2, true)
        )

        // Create monitor contended entered requests to use for testing
        (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorContendedEnteredSupport.createMonitorContendedEnteredRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingMonitorContendedEnteredSupport.processAllPendingMonitorContendedEnteredRequests()
        actual should be (expected)
      }
    }

    describe("#pendingMonitorContendedEnteredRequests") {
      it("should return a collection of pending monitor contended entered requests") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, true),
          MonitorContendedEnteredRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          MonitorContendedEnteredRequestInfo(TestRequestId + 2, true)
        )

        (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingMonitorContendedEnteredSupport.createMonitorContendedEnteredRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingMonitorContendedEnteredSupport.pendingMonitorContendedEnteredRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending monitor contended entered requests") {
        val expected = Nil

        // No pending monitor contended entered requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMonitorContendedEnteredSupport.pendingMonitorContendedEnteredRequests

        actual should be (expected)
      }
    }

    describe("#createMonitorContendedEnteredRequestWithId") {
      it("should return Success(id) if the monitor contended entered was created") {
        val expected = Success(TestRequestId)

        // Create a monitor contended entered to use for testing
        (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingMonitorContendedEnteredSupport.createMonitorContendedEnteredRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending monitor contended entered if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending monitor contended entered should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MonitorContendedEnteredRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMonitorContendedEnteredSupport.createMonitorContendedEnteredRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingMonitorContendedEnteredSupport.disablePendingSupport()
        val actual = pendingMonitorContendedEnteredSupport.createMonitorContendedEnteredRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnteredRequest") {
      it("should return true if the monitor contended entered was successfully deleted") {
        val expected = true

        (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending monitor contended entered requests
        // (performed by standard removeMonitorContendedEnteredRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorContendedEnteredSupport.removeMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending monitor contended entered request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending monitor contended entered requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            MonitorContendedEnteredRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingMonitorContendedEnteredSupport.removeMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the monitor contended entered request was not found") {
        val expected = false

        (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending monitor contended entered requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingMonitorContendedEnteredSupport.removeMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

