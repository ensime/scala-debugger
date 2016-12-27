package org.scaladebugger.api.lowlevel.methods

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestMethodExitManager}

import scala.util.{Failure, Success}

class PendingMethodExitSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodExitManager = mock[MethodExitManager]

  private class TestMethodExitInfoPendingActionManager
    extends PendingActionManager[MethodExitRequestInfo]
  private val mockPendingActionManager =
    mock[TestMethodExitInfoPendingActionManager]

  private val pendingMethodExitSupport = new TestMethodExitManager(
    mockMethodExitManager
  ) with PendingMethodExitSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MethodExitRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMethodExitSupport") {
    describe("#processAllPendingMethodExitRequests") {
      it("should process all pending method exit requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Seq(
          MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodExitRequestInfo(TestRequestId + 1, true, testClassName + 1, testMethodName),
          MethodExitRequestInfo(TestRequestId + 2, true, testClassName, testMethodName + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingMethodExitSupport.processAllPendingMethodExitRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingMethodExitRequestsForClass") {
      it("should process pending method exit requests for the specified class") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Seq(
          MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodExitRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1)
        )
        val actions = (expected :+ MethodExitRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName))
          .map(ActionInfo.apply("", _: MethodExitRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[MethodExitRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingMethodExitSupport.processPendingMethodExitRequestsForClass(
          testClassName
        )

        actual should be (expected)
      }
    }

    describe("#pendingMethodExitRequests") {
      it("should return a collection of all pending method exit requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val expected = Seq(
          MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodExitRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1),
          MethodExitRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName)
        )

        val actions = expected.map(ActionInfo.apply("", _: MethodExitRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[MethodExitRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingMethodExitSupport.pendingMethodExitRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending method exit requests") {
        val expected = Nil

        // No pending method exit requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodExitSupport.pendingMethodExitRequests

        actual should be (expected)
      }
    }

    describe("#pendingMethodExitRequestsForClass") {
      it("should return a collection of pending method exit requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val expected = Seq(
          MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodExitRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1)
        )
        val actions = (expected :+ MethodExitRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName))
          .map(ActionInfo.apply("", _: MethodExitRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[MethodExitRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingMethodExitSupport.pendingMethodExitRequestsForClass(testClassName)

        actual should be (expected)
      }

      it("should be empty if there are no pending method exit requests") {
        val expected = Nil

        // No pending method exit requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodExitSupport.pendingMethodExitRequestsForClass("class")

        actual should be (expected)
      }
    }

    describe("#createMethodExitRequestWithId") {
      it("should return Success(id) if the method exit request was created") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Success(TestRequestId)

        // Create a method exit request to use for testing
        (mockMethodExitManager.createMethodExitRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, Nil)
          .returning(expected).once()

        val actual = pendingMethodExitSupport.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should add a pending method exit request if exception thrown") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Success(TestRequestId)

        // Create a method exit request to use for testing
        (mockMethodExitManager.createMethodExitRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending method exit request should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMethodExitSupport.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockMethodExitManager.createMethodExitRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingMethodExitSupport.disablePendingSupport()
        val actual = pendingMethodExitSupport.createMethodExitRequestWithId(
          TestRequestId, testClassName, testMethodName, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequestWithId") {
      it("should return true if the method exit request was successfully deleted") {
        val expected = true

        (mockMethodExitManager.removeMethodExitRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending method exit requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending method exit request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        // Return removals for pending method exit requests
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
            () => {}
          )
        ))
        (mockMethodExitManager.removeMethodExitRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the method exit request was not found") {
        val expected = false

        (mockMethodExitManager.removeMethodExitRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending method exit requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequest") {
      it("should return true if the method exit request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        (mockMethodExitManager.removeMethodExitRequest _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        // Return "no removals" for pending method exit requests (performed by standard
        // removeMethodExitRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should return true if the pending method exit request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        // Return removals for pending method exit requests
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            MethodExitRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
            () => {}
          )
        )
        (mockMethodExitManager.removeMethodExitRequest _)
          .expects(testClassName, testMethodName)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[MethodExitRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should return false if the method exit request was not found") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        (mockMethodExitManager.removeMethodExitRequest _)
          .expects(testClassName, testMethodName)
          .returning(false).once()

        // Return "no removals" for pending method exit requests
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodExitSupport.removeMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }
  }
}

