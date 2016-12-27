package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestStepManager}

import scala.util.{Failure, Success, Try}

class PendingStepSupportSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockStepManager = mock[StepManager]

  private class TestStepInfoPendingActionManager
    extends PendingActionManager[StepRequestInfo]
  private val mockPendingActionManager =
    mock[TestStepInfoPendingActionManager]

  private val pendingStepSupport = new TestStepManager(
    mockStepManager
  ) with PendingStepSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[StepRequestInfo] =
      mockPendingActionManager
  }

  // NOTE: This is provided to get around issue with ScalaMock unable to
  //       mock an overloaded method
  private val mockCreateStepRequestWithId =
    mockFunction[String, Boolean, ThreadReference, Int, Int, Seq[JDIRequestArgument], Try[String]]
  private class MockedCreateStepManager extends TestStepManager(mockStepManager) {
    override def createStepRequestWithId(
      requestId: String,
      removeExistingRequests: Boolean,
      threadReference: ThreadReference,
      size: Int,
      depth: Int,
      extraArguments: JDIRequestArgument*
    ): Try[String] = mockCreateStepRequestWithId(
      requestId,
      removeExistingRequests,
      threadReference,
      size,
      depth,
      extraArguments
    )
  }
  private val pendingStepSupportWithMockedCreate = new TestStepManager(
    new MockedCreateStepManager
  ) with PendingStepSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[StepRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingStepSupport") {
    describe("#processAllPendingStepRequests") {
      it("should process all pending step requests") {
        val mockThreadReference = mock[ThreadReference]
        val testRemoveExistingRequests = true
        val testSize = 0
        val testDepth = 1

        val expected = Seq(
          StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth),
          StepRequestInfo(TestRequestId + 1, true, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth),
          StepRequestInfo(TestRequestId + 2, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth + 1)
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingStepSupport.processAllPendingStepRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingStepRequestsForThread") {
      it("should process pending step requests for the specified class") {
        val mockThreadReference = mock[ThreadReference]
        val testRemoveExistingRequests = true
        val testSize = 0
        val testDepth = 1

        val expected = Seq(
          StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth),
          StepRequestInfo(TestRequestId + 1, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth + 1)
        )
        val actions = (expected :+ StepRequestInfo(TestRequestId + 2, true, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth))
          .map(ActionInfo.apply("", _: StepRequestInfo, () => {}))

        // Return our data that represents the processed actions
        (mockPendingActionManager.processActions _).expects(*).onCall(
          (f: ActionInfo[StepRequestInfo] => Boolean) => actions.filter(f)
        ).once()

        val actual = pendingStepSupport.processPendingStepRequestsForThread(
          mockThreadReference
        )

        actual should be (expected)
      }
    }

    describe("#pendingStepRequests") {
      it("should return a collection of all pending step requests") {
        val testRemoveExistingRequests = true
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val expected = Seq(
          StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth),
          StepRequestInfo(TestRequestId + 1, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth + 1),
          StepRequestInfo(TestRequestId + 2, true, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth)
        )

        val actions = expected.map(ActionInfo.apply("", _: StepRequestInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[StepRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingStepSupport.pendingStepRequests

        actual should be (expected)
      }

      it("should be empty if there are no pending step requests") {
        val expected = Nil

        // No pending step requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingStepSupport.pendingStepRequests

        actual should be (expected)
      }
    }

    describe("#pendingStepRequestsForThread") {
      it("should return a collection of pending step requests") {
        val testRemoveExistingRequests = true
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val expected = Seq(
          StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth),
          StepRequestInfo(TestRequestId + 1, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth + 1)
        )
        val actions = (expected :+ StepRequestInfo(TestRequestId + 2, true, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth))
          .map(ActionInfo.apply("", _: StepRequestInfo, () => {}))

        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[StepRequestInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingStepSupport.pendingStepRequestsForThread(mockThreadReference)

        actual should be (expected)
      }

      it("should be empty if there are no pending step requests") {
        val expected = Nil

        // No pending step requests
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingStepSupport.pendingStepRequestsForThread(
          mock[ThreadReference]
        )

        actual should be (expected)
      }
    }

    describe("#createStepRequestWithId") {
      it("should return Success(id) if the step request was created") {
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        val expected = Success(TestRequestId)

        // Create a step request to use for testing
        mockCreateStepRequestWithId
          .expects(TestRequestId, true, mockThreadReference, testSize, testDepth, Nil)
          .returning(expected).once()

        val actual = pendingStepSupportWithMockedCreate.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize, testDepth
        )

        actual should be (expected)
      }

      it("should add a pending step request if exception thrown") {
        val testRemoveExistingRequests = true
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        val expected = Success(TestRequestId)

        // Create a step request to use for testing
        mockCreateStepRequestWithId
          .expects(TestRequestId, true, mockThreadReference, testSize, testDepth, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending step request should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingStepSupportWithMockedCreate.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize, testDepth
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val extraArguments = Seq(stub[JDIRequestArgument])

        mockCreateStepRequestWithId.expects(*, *, *, *, *, *)
          .returning(expected).once()

        pendingStepSupportWithMockedCreate.disablePendingSupport()
        val actual = pendingStepSupportWithMockedCreate.createStepRequestWithId(
          TestRequestId, mockThreadReference, testSize, testDepth, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeStepRequestWithId") {
      it("should return true if the step request was successfully deleted") {
        val expected = true

        (mockStepManager.removeStepRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending step requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingStepSupport.removeStepRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending step request was successfully deleted") {
        val expected = true

        val testRemoveExistingRequests = true
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        // Return removals for pending step requests
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth, Nil),
            () => {}
          )
        ))
        (mockStepManager.removeStepRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingStepSupport.removeStepRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the step request was not found") {
        val expected = false

        (mockStepManager.removeStepRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending step requests
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingStepSupport.removeStepRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeStepRequest") {
      it("should return true if the step request was successfully deleted") {
        val expected = true

        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        (mockStepManager.removeStepRequest _)
          .expects(mockThreadReference)
          .returning(true).once()

        // Return "no removals" for pending step requests (performed by standard
        // removeStepRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingStepSupport.removeStepRequest(
          mockThreadReference
        )

        actual should be (expected)
      }

      it("should return true if the pending step request was successfully deleted") {
        val expected = true

        val testRemoveExistingRequests = true
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        // Return removals for pending step requests
        val actions = Seq(
          ActionInfo(
            TestRequestId,
            StepRequestInfo(TestRequestId, true, testRemoveExistingRequests, mockThreadReference, testSize, testDepth, Nil),
            () => {}
          )
        )
        (mockStepManager.removeStepRequest _)
          .expects(mockThreadReference)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _).expects(*).onCall(
          (f: ActionInfo[StepRequestInfo] => Boolean) =>
            actions.filter(f)
        ).once()

        val actual = pendingStepSupport.removeStepRequest(
          mockThreadReference
        )

        actual should be (expected)
      }

      it("should return false if the step request was not found") {
        val expected = false
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        (mockStepManager.removeStepRequest _)
          .expects(mockThreadReference)
          .returning(false).once()

        // Return "no removals" for pending step requests
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingStepSupport.removeStepRequest(
          mockThreadReference
        )

        actual should be (expected)
      }
    }
  }
}
