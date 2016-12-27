package org.scaladebugger.api.lowlevel.exceptions

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestExceptionManager}

import scala.util.{Failure, Success}

class PendingExceptionSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockExceptionManager = mock[ExceptionManager]

  private class TestExceptionInfoPendingActionManager
    extends PendingActionManager[ExceptionRequestInfo]
  private val mockPendingActionManager =
    mock[TestExceptionInfoPendingActionManager]

  private val pendingExceptionSupport = new TestExceptionManager(
    mockExceptionManager
  ) with PendingExceptionSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[ExceptionRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingExceptionSupport") {
    describe("#processAllPendingExceptionRequests") {
      it("should process all pending exceptions") {
        val testClassName = "some.class.name"

        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, true, testClassName, true, false),
          ExceptionRequestInfo(TestRequestId + 1, true, testClassName + 1, true, false),
          ExceptionRequestInfo(TestRequestId + 2, true, testClassName, false, true)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingExceptionSupport.processAllPendingExceptionRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingExceptionRequestsForClass") {
      it("should process pending exceptions for the specified class") {
        val testClassName = "some.class.name"

        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, true, testClassName, true, false),
          ExceptionRequestInfo(TestRequestId + 1, true, testClassName, false, true)
        )
        val actions = (expected :+ ExceptionRequestInfo(TestRequestId + 2, true, testClassName + 1, true, false))
          .map(ActionInfo.apply("", _: ExceptionRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[ExceptionRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingExceptionSupport.processPendingExceptionRequestsForClass(
          testClassName
        )

        actual should be (expected)
      }
    }

    describe("#pendingExceptionRequests") {
      it("should return a collection of all pending exceptions") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, true, "class1", true, false),
          ExceptionRequestInfo(TestRequestId + 1, true, "class1", false, true),
          ExceptionRequestInfo(TestRequestId + 2, true, "class2", true, false)
        )

        val actions = expected.map(ActionInfo.apply("", _: ExceptionRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[ExceptionRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingExceptionSupport.pendingExceptionRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending exceptions") {
        val expected = Nil

        // No pending exceptions
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingExceptionSupport.pendingExceptionRequests

        actual should be (expected)
      }
    }

    describe("#pendingExceptionRequestsForClass") {
      it("should return a collection of pending exceptions") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, true, "class1", true, false),
          ExceptionRequestInfo(TestRequestId + 1, true, "class1", false, true)
        )
        val actions = (expected :+ ExceptionRequestInfo(TestRequestId + 2, true, "class2", true, false))
          .map(ActionInfo.apply("", _: ExceptionRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[ExceptionRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingExceptionSupport.pendingExceptionRequestsForClass("class1")

        actual should be (expected)
      }

      it("should be empty if there are no pending exceptions") {
        val expected = Nil

        // No pending exceptions
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingExceptionSupport.pendingExceptionRequestsForClass("class")

        actual should be (expected)
      }
    }

    describe("#createCatchallExceptionRequestWithId") {
      it("should return Success(id) if the exception was created") {
        val expected = Success(TestRequestId)

        // Create a exception to use for testing
        (mockExceptionManager.createCatchallExceptionRequestWithId _)
          .expects(TestRequestId, true, false, Nil)
          .returning(expected).once()

        val actual = pendingExceptionSupport.createCatchallExceptionRequestWithId(
          TestRequestId,
          true,
          false
        )

        actual should be (expected)
      }

      it("should add a pending exception request if exception thrown") {
        val expected = Success(TestRequestId)

        // Create a exception to use for testing
        (mockExceptionManager.createCatchallExceptionRequestWithId _)
          .expects(TestRequestId, true, false, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending exception should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          ExceptionRequestInfo(
            TestRequestId,
            true,
            ExceptionRequestInfo.DefaultCatchallExceptionName,
            true,
            false,
            Nil
          ),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingExceptionSupport.createCatchallExceptionRequestWithId(
          TestRequestId,
          true,
          false
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockExceptionManager.createCatchallExceptionRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingExceptionSupport.disablePendingSupport()
        val actual = pendingExceptionSupport.createCatchallExceptionRequestWithId(
          TestRequestId, true, false, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createExceptionRequestWithId") {
      it("should throw an exception if the exception name is null") {
        intercept[IllegalArgumentException] {
          pendingExceptionSupport.createExceptionRequestWithId(
            "id",
            null,
            true,
            false
          )
        }
      }

      it("should return Success(id) if the exception was created") {
        val testClassName = "some.class.name"

        val expected = Success(TestRequestId)

        // Create a exception to use for testing
        (mockExceptionManager.createExceptionRequestWithId _)
          .expects(TestRequestId, testClassName, true, false, Nil)
          .returning(expected).once()

        val actual = pendingExceptionSupport.createExceptionRequestWithId(
          TestRequestId,
          testClassName,
          true, false
        )

        actual should be (expected)
      }

      it("should add a pending exception request if exception thrown") {
        val testClassName = "some.class.name"

        val expected = Success(TestRequestId)

        // Create a exception to use for testing
        (mockExceptionManager.createExceptionRequestWithId _)
          .expects(TestRequestId, testClassName, true, false, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending exception should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          ExceptionRequestInfo(TestRequestId, true, testClassName, true, false, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingExceptionSupport.createExceptionRequestWithId(
          TestRequestId,
          testClassName,
          true, false
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testClassName = "some.class.name"
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockExceptionManager.createExceptionRequestWithId _)
          .expects(*, *, *, *, *)
          .returning(expected).once()

        pendingExceptionSupport.disablePendingSupport()
        val actual = pendingExceptionSupport.createExceptionRequestWithId(
          TestRequestId, testClassName, true, false, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequestWithId") {
      it("should return true if the exception was successfully deleted") {
        val expected = true

        (mockExceptionManager.removeExceptionRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending exceptions
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingExceptionSupport.removeExceptionRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending exception was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"

        // Return removals for pending exceptions
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            ExceptionRequestInfo(testClassName, true, TestRequestId, true, false, Nil),
            () => {}
          )
        ))
        (mockExceptionManager.removeExceptionRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingExceptionSupport.removeExceptionRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the exception was not found") {
        val expected = false

        (mockExceptionManager.removeExceptionRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending exceptions
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingExceptionSupport.removeExceptionRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequest") {
      it("should return true if the exception was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"

        (mockExceptionManager.removeExceptionRequest _)
          .expects(testClassName)
          .returning(true).once()

        // Return "no removals" for pending exceptions (performed by standard
        // removeExceptionRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingExceptionSupport.removeExceptionRequest(
          testClassName
        )

        actual should be (expected)
      }

      it("should return true if the pending exception was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"

        // Return removals for pending exceptions
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            ExceptionRequestInfo(TestRequestId, true, testClassName, true, false, Nil),
            () => {}
          )
        )
        (mockExceptionManager.removeExceptionRequest _)
          .expects(testClassName)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[ExceptionRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingExceptionSupport.removeExceptionRequest(
          testClassName
        )

        actual should be (expected)
      }

      it("should return false if the exception was not found") {
        val expected = false
        val testClassName = "some.class.name"

        (mockExceptionManager.removeExceptionRequest _)
          .expects(testClassName)
          .returning(false).once()

        // Return "no removals" for pending exceptions
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingExceptionSupport.removeExceptionRequest(
          testClassName
        )

        actual should be (expected)
      }
    }
  }
}
