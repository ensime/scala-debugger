package org.scaladebugger.api.lowlevel.watchpoints
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestAccessWatchpointManager}

import scala.util.{Failure, Success}

class PendingAccessWatchpointSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockAccessWatchpointManager = mock[AccessWatchpointManager]

  private class TestAccessWatchpointInfoPendingActionManager
    extends PendingActionManager[AccessWatchpointRequestInfo]
  private val mockPendingActionManager =
    mock[TestAccessWatchpointInfoPendingActionManager]

  private val pendingAccessWatchpointSupport = new TestAccessWatchpointManager(
    mockAccessWatchpointManager
  ) with PendingAccessWatchpointSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[AccessWatchpointRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingAccessWatchpointSupport") {
    describe("#processAllPendingAccessWatchpointRequests") {
      it("should process all pending access watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Seq(
          AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          AccessWatchpointRequestInfo(TestRequestId + 1, true, testClassName + 1, testFieldName),
          AccessWatchpointRequestInfo(TestRequestId + 2, true, testClassName, testFieldName + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingAccessWatchpointSupport.processAllPendingAccessWatchpointRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingAccessWatchpointRequestsForClass") {
      it("should process pending access watchpoint requests for the specified class") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Seq(
          AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          AccessWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1)
        )
        val actions = (expected :+ AccessWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName))
          .map(ActionInfo.apply("", _: AccessWatchpointRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[AccessWatchpointRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingAccessWatchpointSupport.processPendingAccessWatchpointRequestsForClass(
          testClassName
        )

        actual should be (expected)
      }
    }

    describe("#pendingAccessWatchpointRequests") {
      it("should return a collection of all pending access watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val expected = Seq(
          AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          AccessWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1),
          AccessWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName)
        )

        val actions = expected.map(ActionInfo.apply("", _: AccessWatchpointRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[AccessWatchpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingAccessWatchpointSupport.pendingAccessWatchpointRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending access watchpoint requests") {
        val expected = Nil

        // No pending access watchpoint requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingAccessWatchpointSupport.pendingAccessWatchpointRequests

        actual should be (expected)
      }
    }

    describe("#pendingAccessWatchpointRequestsForClass") {
      it("should return a collection of pending access watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val expected = Seq(
          AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          AccessWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1)
        )
        val actions = (expected :+ AccessWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName))
          .map(ActionInfo.apply("", _: AccessWatchpointRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[AccessWatchpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingAccessWatchpointSupport.pendingAccessWatchpointRequestsForClass(testClassName)

        actual should be (expected)
      }

      it("should be empty if there are no pending access watchpoint requests") {
        val expected = Nil

        // No pending access watchpoint requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingAccessWatchpointSupport.pendingAccessWatchpointRequestsForClass("class")

        actual should be (expected)
      }
    }

    describe("#createAccessWatchpointRequestWithId") {
      it("should return Success(id) if the access watchpoint request was created") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Success(TestRequestId)

        // Create a access watchpoint request to use for testing
        (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, Nil)
          .returning(expected).once()

        val actual = pendingAccessWatchpointSupport.createAccessWatchpointRequestWithId(
          TestRequestId,
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should add a pending access watchpoint request if exception thrown") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Success(TestRequestId)

        // Create a access watchpoint request to use for testing
        (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending access watchpoint request should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingAccessWatchpointSupport.createAccessWatchpointRequestWithId(
          TestRequestId,
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingAccessWatchpointSupport.disablePendingSupport()
        val actual = pendingAccessWatchpointSupport.createAccessWatchpointRequestWithId(
          TestRequestId, testClassName, testFieldName, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequestWithId") {
      it("should return true if the access watchpoint request was successfully deleted") {
        val expected = true

        (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending access watchpoint requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending access watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        // Return removals for pending access watchpoint requests
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
            () => {}
          )
        ))
        (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the access watchpoint request was not found") {
        val expected = false

        (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending access watchpoint requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequest") {
      it("should return true if the access watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        (mockAccessWatchpointManager.removeAccessWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(true).once()

        // Return "no removals" for pending access watchpoint requests (performed by standard
        // removeAccessWatchpointRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should return true if the pending access watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        // Return removals for pending access watchpoint requests
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            AccessWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
            () => {}
          )
        )
        (mockAccessWatchpointManager.removeAccessWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[AccessWatchpointRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should return false if the access watchpoint request was not found") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        (mockAccessWatchpointManager.removeAccessWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(false).once()

        // Return "no removals" for pending access watchpoint requests
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingAccessWatchpointSupport.removeAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }
  }
}
