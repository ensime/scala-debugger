package org.scaladebugger.api.lowlevel.classes

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestClassPrepareManager}

import scala.util.{Failure, Success}

class PendingClassPrepareSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassPrepareManager = mock[ClassPrepareManager]

  private class TestClassPrepareInfoPendingActionManager
    extends PendingActionManager[ClassPrepareRequestInfo]
  private val mockPendingActionManager =
    mock[TestClassPrepareInfoPendingActionManager]

  private val pendingClassPrepareSupport = new TestClassPrepareManager(
    mockClassPrepareManager
  ) with PendingClassPrepareSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[ClassPrepareRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingClassPrepareSupport") {
    describe("#processAllPendingClassPrepareRequests") {
      it("should process all pending class prepare requests") {
        val expected = Seq(
          ClassPrepareRequestInfo(TestRequestId, true),
          ClassPrepareRequestInfo(TestRequestId + 1, true),
          ClassPrepareRequestInfo(TestRequestId + 2, true)
        )

        // Create class prepare requests to use for testing
        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingClassPrepareSupport.createClassPrepareRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(c => ActionInfo("id", c, () => {}))).once()

        val actual = pendingClassPrepareSupport.processAllPendingClassPrepareRequests()
        actual should be (expected)
      }
    }

    describe("#pendingClassPrepareRequests") {
      it("should return a collection of pending class prepare requests") {
        val expected = Seq(
          ClassPrepareRequestInfo(TestRequestId, true),
          ClassPrepareRequestInfo(TestRequestId + 1, true, Seq(stub[JDIRequestArgument])),
          ClassPrepareRequestInfo(TestRequestId + 2, true)
        )

        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(*, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(c => pendingClassPrepareSupport.createClassPrepareRequest(
          c.extraArguments: _*
        ))

        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(expected).once()

        val actual = pendingClassPrepareSupport.pendingClassPrepareRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending class prepare requests") {
        val expected = Nil

        // No pending class prepare requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingClassPrepareSupport.pendingClassPrepareRequests

        actual should be (expected)
      }
    }

    describe("#createClassPrepareRequestWithId") {
      it("should return Success(id) if the class prepare was created") {
        val expected = Success(TestRequestId)

        // Create a class prepare to use for testing
        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(TestRequestId, Nil)
          .returning(expected).once()

        val actual = pendingClassPrepareSupport.createClassPrepareRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should add a pending class prepare if exception thrown") {
        val expected = Success(TestRequestId)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(*, *)
          .returning(Failure(new Throwable)).once()

        // Pending class prepare should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          ClassPrepareRequestInfo(TestRequestId, true, extraArguments),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingClassPrepareSupport.createClassPrepareRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(*, *)
          .returning(expected).once()

        pendingClassPrepareSupport.disablePendingSupport()
        val actual = pendingClassPrepareSupport.createClassPrepareRequestWithId(
          TestRequestId, extraArguments: _*
        )

        actual should be (expected)
      }
    }
    describe("#removeClassPrepareRequest") {
      it("should return true if the class prepare was successfully deleted") {
        val expected = true

        (mockClassPrepareManager.removeClassPrepareRequest _).expects(*)
          .returning(true).once()

        // Return "no removals" for pending class prepare requests
        // (performed by standard removeClassPrepareRequest call)
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingClassPrepareSupport.removeClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending class prepare request was successfully deleted") {
        val expected = true
        val extraArguments = Seq(stub[JDIRequestArgument])

        // Return removals for pending class prepare requests
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            ClassPrepareRequestInfo(TestRequestId, true, extraArguments),
            () => {}
          )
        )
        (mockClassPrepareManager.removeClassPrepareRequest _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(Some(pendingRemovalReturn)).once()

        val actual = pendingClassPrepareSupport.removeClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the class prepare request was not found") {
        val expected = false

        (mockClassPrepareManager.removeClassPrepareRequest _)
          .expects(*)
          .returning(false).once()

        // Return "no removals" for pending class prepare requests
        (mockPendingActionManager.removePendingActionsWithId _).expects(*)
          .returning(None).once()

        val actual = pendingClassPrepareSupport.removeClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
