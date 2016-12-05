package org.scaladebugger.api.lowlevel.methods

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestMethodEntryManager}

import scala.util.{Failure, Success}

class PendingMethodEntrySupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodEntryManager = mock[MethodEntryManager]

  private class TestMethodEntryInfoPendingActionManager
    extends PendingActionManager[MethodEntryRequestInfo]
  private val mockPendingActionManager =
    mock[TestMethodEntryInfoPendingActionManager]

  private val pendingMethodEntrySupport = new TestMethodEntryManager(
    mockMethodEntryManager
  ) with PendingMethodEntrySupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[MethodEntryRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingMethodEntrySupport") {
    describe("#processAllPendingMethodEntryRequests") {
      it("should process all pending method entry requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Seq(
          MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodEntryRequestInfo(TestRequestId + 1, true, testClassName + 1, testMethodName),
          MethodEntryRequestInfo(TestRequestId + 2, true, testClassName, testMethodName + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingMethodEntrySupport.processAllPendingMethodEntryRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingMethodEntryRequestsForClass") {
      it("should process pending method entry requests for the specified class") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Seq(
          MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodEntryRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1)
        )
        val actions = (expected :+ MethodEntryRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName))
          .map(ActionInfo.apply("", _: MethodEntryRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[MethodEntryRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingMethodEntrySupport.processPendingMethodEntryRequestsForClass(
          testClassName
        )

        actual should be (expected)
      }
    }

    describe("#pendingMethodEntryRequests") {
      it("should return a collection of all pending method entry requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val expected = Seq(
          MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodEntryRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1),
          MethodEntryRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName)
        )

        val actions = expected.map(ActionInfo.apply("", _: MethodEntryRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[MethodEntryRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingMethodEntrySupport.pendingMethodEntryRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending method entry requests") {
        val expected = Nil

        // No pending method entry requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodEntrySupport.pendingMethodEntryRequests

        actual should be (expected)
      }
    }

    describe("#pendingMethodEntryRequestsForClass") {
      it("should return a collection of pending method entry requests") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val expected = Seq(
          MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName),
          MethodEntryRequestInfo(TestRequestId + 1, true, testClassName, testMethodName + 1)
        )
        val actions = (expected :+ MethodEntryRequestInfo(TestRequestId + 2, true, testClassName + 1, testMethodName))
          .map(ActionInfo.apply("", _: MethodEntryRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[MethodEntryRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingMethodEntrySupport.pendingMethodEntryRequestsForClass(testClassName)

        actual should be (expected)
      }

      it("should be empty if there are no pending method entry requests") {
        val expected = Nil

        // No pending method entry requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodEntrySupport.pendingMethodEntryRequestsForClass("class")

        actual should be (expected)
      }
    }

    describe("#createMethodEntryRequestWithId") {
      it("should return Success(id) if the method entry request was created") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Success(TestRequestId)

        // Create a method entry request to use for testing
        (mockMethodEntryManager.createMethodEntryRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, Nil)
          .returning(expected).once()

        val actual = pendingMethodEntrySupport.createMethodEntryRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should add a pending method entry request if exception thrown") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val expected = Success(TestRequestId)

        // Create a method entry request to use for testing
        (mockMethodEntryManager.createMethodEntryRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending method entry request should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingMethodEntrySupport.createMethodEntryRequestWithId(
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

        (mockMethodEntryManager.createMethodEntryRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingMethodEntrySupport.disablePendingSupport()
        val actual = pendingMethodEntrySupport.createMethodEntryRequestWithId(
          TestRequestId, testClassName, testMethodName, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequestWithId") {
      it("should return true if the method entry request was successfully deleted") {
        val expected = true

        (mockMethodEntryManager.removeMethodEntryRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending method entry requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending method entry request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        // Return removals for pending method entry requests
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
            () => {}
          )
        ))
        (mockMethodEntryManager.removeMethodEntryRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the method entry request was not found") {
        val expected = false

        (mockMethodEntryManager.removeMethodEntryRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending method entry requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequest") {
      it("should return true if the method entry request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        (mockMethodEntryManager.removeMethodEntryRequest _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        // Return "no removals" for pending method entry requests (performed by standard
        // removeMethodEntryRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should return true if the pending method entry request was successfully deleted") {
        val expected = true

        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        // Return removals for pending method entry requests
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            MethodEntryRequestInfo(TestRequestId, true, testClassName, testMethodName, Nil),
            () => {}
          )
        )
        (mockMethodEntryManager.removeMethodEntryRequest _)
          .expects(testClassName, testMethodName)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[MethodEntryRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }

      it("should return false if the method entry request was not found") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        (mockMethodEntryManager.removeMethodEntryRequest _)
          .expects(testClassName, testMethodName)
          .returning(false).once()

        // Return "no removals" for pending method entry requests
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingMethodEntrySupport.removeMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }
  }
}
