package org.scaladebugger.api.lowlevel.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestModificationWatchpointManager}

import scala.util.{Failure, Success}

class PendingModificationWatchpointSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockModificationWatchpointManager = mock[ModificationWatchpointManager]

  private class TestModificationWatchpointInfoPendingActionManager
    extends PendingActionManager[ModificationWatchpointRequestInfo]
  private val mockPendingActionManager =
    mock[TestModificationWatchpointInfoPendingActionManager]

  private val pendingModificationWatchpointSupport = new TestModificationWatchpointManager(
    mockModificationWatchpointManager
  ) with PendingModificationWatchpointSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[ModificationWatchpointRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingModificationWatchpointSupport") {
    describe("#processAllPendingModificationWatchpointRequests") {
      it("should process all pending modification watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Seq(
          ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          ModificationWatchpointRequestInfo(TestRequestId + 1, true, testClassName + 1, testFieldName),
          ModificationWatchpointRequestInfo(TestRequestId + 2, true, testClassName, testFieldName + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingModificationWatchpointSupport.processAllPendingModificationWatchpointRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingModificationWatchpointRequestsForClass") {
      it("should process pending modification watchpoint requests for the specified class") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Seq(
          ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          ModificationWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1)
        )
        val actions = (expected :+ ModificationWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName))
          .map(ActionInfo.apply("", _: ModificationWatchpointRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[ModificationWatchpointRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingModificationWatchpointSupport.processPendingModificationWatchpointRequestsForClass(
          testClassName
        )

        actual should be (expected)
      }
    }

    describe("#pendingModificationWatchpointRequests") {
      it("should return a collection of all pending modification watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val expected = Seq(
          ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          ModificationWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1),
          ModificationWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName)
        )

        val actions = expected.map(ActionInfo.apply("", _: ModificationWatchpointRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[ModificationWatchpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingModificationWatchpointSupport.pendingModificationWatchpointRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending modification watchpoint requests") {
        val expected = Nil

        // No pending modification watchpoint requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingModificationWatchpointSupport.pendingModificationWatchpointRequests

        actual should be (expected)
      }
    }

    describe("#pendingModificationWatchpointRequestsForClass") {
      it("should return a collection of pending modification watchpoint requests") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val expected = Seq(
          ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName),
          ModificationWatchpointRequestInfo(TestRequestId + 1, true, testClassName, testFieldName + 1)
        )
        val actions = (expected :+ ModificationWatchpointRequestInfo(TestRequestId + 2, true, testClassName + 1, testFieldName))
          .map(ActionInfo.apply("", _: ModificationWatchpointRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[ModificationWatchpointRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingModificationWatchpointSupport.pendingModificationWatchpointRequestsForClass(testClassName)

        actual should be (expected)
      }

      it("should be empty if there are no pending modification watchpoint requests") {
        val expected = Nil

        // No pending modification watchpoint requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingModificationWatchpointSupport.pendingModificationWatchpointRequestsForClass("class")

        actual should be (expected)
      }
    }

    describe("#createModificationWatchpointRequestWithId") {
      it("should return Success(id) if the modification watchpoint request was created") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Success(TestRequestId)

        // Create a modification watchpoint request to use for testing
        (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, Nil)
          .returning(expected).once()

        val actual = pendingModificationWatchpointSupport.createModificationWatchpointRequestWithId(
          TestRequestId,
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should add a pending modification watchpoint request if exception thrown") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val expected = Success(TestRequestId)

        // Create a modification watchpoint request to use for testing
        (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending modification watchpoint request should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingModificationWatchpointSupport.createModificationWatchpointRequestWithId(
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

        (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingModificationWatchpointSupport.disablePendingSupport()
        val actual = pendingModificationWatchpointSupport.createModificationWatchpointRequestWithId(
          TestRequestId, testClassName, testFieldName, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequestWithId") {
      it("should return true if the modification watchpoint request was successfully deleted") {
        val expected = true

        (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending modification watchpoint requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending modification watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        // Return removals for pending modification watchpoint requests
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
            () => {}
          )
        ))
        (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the modification watchpoint request was not found") {
        val expected = false

        (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending modification watchpoint requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequest") {
      it("should return true if the modification watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        (mockModificationWatchpointManager.removeModificationWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(true).once()

        // Return "no removals" for pending modification watchpoint requests (performed by standard
        // removeModificationWatchpointRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should return true if the pending modification watchpoint request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        // Return removals for pending modification watchpoint requests
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            ModificationWatchpointRequestInfo(TestRequestId, true, testClassName, testFieldName, Nil),
            () => {}
          )
        )
        (mockModificationWatchpointManager.removeModificationWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[ModificationWatchpointRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }

      it("should return false if the modification watchpoint request was not found") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        (mockModificationWatchpointManager.removeModificationWatchpointRequest _)
          .expects(testClassName, testFieldName)
          .returning(false).once()

        // Return "no removals" for pending modification watchpoint requests
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingModificationWatchpointSupport.removeModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }
  }
}

