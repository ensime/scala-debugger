package org.scaladebugger.api.lowlevel.breakpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestBreakpointManager}

import scala.util.{Failure, Success}

class PendingBreakpointSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockBreakpointManager = mock[BreakpointManager]

  private class TestBreakpointInfoPendingActionManager
    extends PendingActionManager[BreakpointRequestInfo]
  private val mockPendingActionManager =
    mock[TestBreakpointInfoPendingActionManager]

  private val pendingBreakpointSupport = new TestBreakpointManager(
    mockBreakpointManager
  ) with PendingBreakpointSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[BreakpointRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingBreakpointSupport") {
    describe("#processAllPendingBreakpointRequests") {
      it("should process all pending breakpoints") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq(
          BreakpointRequestInfo("", true, testFileName, testLineNumber),
          BreakpointRequestInfo("", true, testFileName + 1, testLineNumber),
          BreakpointRequestInfo("", true, testFileName, testLineNumber + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingBreakpointSupport.processAllPendingBreakpointRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingBreakpointRequestsForFile") {
      it("should process pending breakpoints for the specified file") {
        val expected = Seq(
          BreakpointRequestInfo("", true, "file1", 1),
          BreakpointRequestInfo("", true, "file1", 999)
        )
        val actions = (expected :+ BreakpointRequestInfo("", true, "file2", 1))
          .map(ActionInfo.apply("", _: BreakpointRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[BreakpointRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingBreakpointSupport.processPendingBreakpointRequestsForFile(
          "file1"
        )

        actual should be (expected)
      }
    }

    describe("#pendingBreakpointRequests") {
      it("should return a collection of all pending breakpoints") {
        val expected = Seq(
          BreakpointRequestInfo("", true, "file1", 1),
          BreakpointRequestInfo("", true, "file1", 999),
          BreakpointRequestInfo("", true, "file2", 1)
        )

        val actions = expected.map(ActionInfo.apply("", _: BreakpointRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[BreakpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingBreakpointSupport.pendingBreakpointRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending breakpoints") {
        val expected = Nil

        // No pending breakpoints
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.pendingBreakpointRequests

        actual should be (expected)
      }
    }

    describe("#pendingBreakpointRequestsForFile") {
      it("should return a collection of pending breakpoints") {
        val expected = Seq(
          BreakpointRequestInfo("", true, "file1", 1),
          BreakpointRequestInfo("", true, "file1", 999)
        )
        val actions = (expected :+ BreakpointRequestInfo("", true, "file2", 1))
          .map(ActionInfo.apply("", _: BreakpointRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[BreakpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingBreakpointSupport.pendingBreakpointRequestsForFile("file1")

        actual should be (expected)
      }

      it("should be empty if there are no pending breakpoints") {
        val expected = Nil

        // No pending breakpoints
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.pendingBreakpointRequestsForFile("file")

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequestWithId") {
      it("should return Success(id) if the breakpoint was created") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(expected).once()

        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should add a pending breakpoint if exception thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointRequestInfo(TestRequestId, true, testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testFileName = "some/file/name"
        val testLineNumber = 1
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingBreakpointSupport.disablePendingSupport()
        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId, testFileName, testLineNumber, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequestWithId") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true

        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        // Return removals for pending breakpoints
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            BreakpointRequestInfo(TestRequestId, true, testFileName, testLineNumber, Nil),
            () => {}
          )
        ))
        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false

        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequest") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(true).once()

        // Return "no removals" for pending breakpoints (performed by standard
        // removeBreakpointRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          1
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        val actions = Seq(
          ActionInfo(
            TestRequestId,
            BreakpointRequestInfo(TestRequestId, true, testFileName, testLineNumber, Nil),
            () => {}
          )
        )
        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[BreakpointRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val testFileName = "some/file/name"
        val testLineNumber = 1

        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(false).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }
    }
  }
}
