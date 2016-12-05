package org.scaladebugger.api.lowlevel.vm

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestVMDeathManager}

import scala.util.{Failure, Success}

class PendingVMDeathSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockVMDeathManager = mock[VMDeathManager]

  private class TestVMDeathInfoPendingActionManager
    extends PendingActionManager[VMDeathRequestInfo]
  private val mockPendingActionManager =
    mock[TestVMDeathInfoPendingActionManager]

  private val pendingVMDeathSupport = new TestVMDeathManager(
    mockVMDeathManager
  ) with PendingVMDeathSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[VMDeathRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingVMDeathSupport") {
    describe("#processAllPendingVMDeathRequests") {
      it("should process all pending vm death requests") {
        val expected = Seq(
          VMDeathRequestInfo(TestRequestId, true),
          VMDeathRequestInfo(TestRequestId + 1, true),
          VMDeathRequestInfo(TestRequestId + 2, true)
        )

        // Create vm death requests to use for testing
        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingVMDeathSupport.createVMDeathRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingVMDeathSupport.processAllPendingVMDeathRequests()
        actual should be (expected)
      }
    }

    describe("#pendingVMDeathRequests") {
      it("should return a collection of pending vm death requests") {
        val expected = Seq(
          VMDeathRequestInfo(TestRequestId, true),
          VMDeathRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          VMDeathRequestInfo(TestRequestId + 2, true)
        )

        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingVMDeathSupport.createVMDeathRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingVMDeathSupport.pendingVMDeathRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending vm death requests") {
        val expected = Nil

        // No pending vm death requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingVMDeathSupport.pendingVMDeathRequests

        actual should be (expected)
      }
    }

    describe("#createVMDeathRequestWithId") {
      it("should return Success(id) if the vm death was created") {
        val expected = Success(TestRequestId)

        // Create a vm death to use for testing
        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingVMDeathSupport.createVMDeathRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending vm death if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending vm death should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          VMDeathRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingVMDeathSupport.createVMDeathRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingVMDeathSupport.disablePendingSupport()
        val actual = pendingVMDeathSupport.createVMDeathRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeVMDeathRequest") {
      it("should return true if the vm death was successfully deleted") {
        val expected = true

        (mockVMDeathManager.removeVMDeathRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending vm death requests
        // (performed by standard removeVMDeathRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingVMDeathSupport.removeVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending vm death request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending vm death requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            VMDeathRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockVMDeathManager.removeVMDeathRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingVMDeathSupport.removeVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the vm death request was not found") {
        val expected = false

        (mockVMDeathManager.removeVMDeathRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending vm death requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingVMDeathSupport.removeVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}

