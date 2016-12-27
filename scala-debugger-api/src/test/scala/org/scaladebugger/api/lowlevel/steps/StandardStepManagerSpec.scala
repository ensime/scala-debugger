package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.{EventRequest, EventRequestManager, StepRequest}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class StandardStepManagerSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadReference = mock[ThreadReference]
  private val mockEventRequestManager = mock[EventRequestManager]

  private val stepManager = new StandardStepManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardStepManager") {
    describe("#createStepOverLineRequest") {
      it("should send a request to step over the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OVER
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepOverLineRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepOverLineRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepOverLineRequest(mockThreadReference)
      }
    }

    describe("#createStepIntoLineRequest") {
      it("should send a request to step into the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_INTO
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepIntoLineRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepIntoLineRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepIntoLineRequest(mockThreadReference)
      }
    }

    describe("#createStepOutLineRequest") {
      it("should send a request to step out of the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OUT
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepOutLineRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepIntoLineRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepIntoLineRequest(mockThreadReference)
      }
    }

    describe("#createStepOverMinRequest") {
      it("should send a request to step over the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_MIN,
          StepRequest.STEP_OVER
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepOverMinRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepOverMinRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepOverMinRequest(mockThreadReference)
      }
    }

    describe("#createStepIntoMinRequest") {
      it("should send a request to step into the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_MIN,
          StepRequest.STEP_INTO
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepIntoMinRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepIntoMinRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepIntoMinRequest(mockThreadReference)
      }
    }

    describe("#createStepOutMinRequest") {
      it("should send a request to step out of the current line of execution") {
        val mockThreadReference = mock[ThreadReference]

        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_MIN,
          StepRequest.STEP_OUT
        ).returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        stepManager.createStepOutMinRequest(mockThreadReference)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepIntoMinRequest(mockThreadReference)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepIntoMinRequest(mockThreadReference)
      }
    }

    describe("#stepRequestListById") {
      it("should contain all step requests in the form of thread reference stored in the manager") {
        val testSize = 0
        val testDepth = 1
        val requestIds = Seq(TestRequestId, TestRequestId + 1, TestRequestId + 2)

        requestIds.foreach { case requestId =>
          val mockThreadReference = mock[ThreadReference]
          (mockEventRequestManager.createStepRequest _)
            .expects(mockThreadReference, testSize, testDepth)
            .returning(stub[StepRequest]).once()
          stepManager.createStepRequestWithId(
            requestId,
            mockThreadReference,
            testSize,
            testDepth
          )
        }

        stepManager.stepRequestListById should
          contain theSameElementsAs (requestIds)
      }
    }

    describe("#stepRequestList") {
      it("should contain all step request information stored in the manager") {
        val testRemoveExistingRequests = true
        val testSize = 0
        val testDepth = 1
        val expected = Seq(
          StepRequestInfo(TestRequestId, false, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth),
          StepRequestInfo(TestRequestId + 1, false, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth),
          StepRequestInfo(TestRequestId + 2, false, testRemoveExistingRequests, mock[ThreadReference], testSize, testDepth)
        )

        // NOTE: Must create a new step manager that does NOT override the
        //       request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val stepManager = new StandardStepManager(mockEventRequestManager)

        expected.foreach { case StepRequestInfo(i, _, r, t, s, d, _) =>
          (mockEventRequestManager.createStepRequest _).expects(t, s, d)
            .returning(stub[StepRequest]).once()
          stepManager.createStepRequestWithId(i, r, t, s, d)
        }

        val actual = stepManager.stepRequestList
        actual should contain theSameElementsAs (expected)
      }
    }

    describe("#createStepRequestWithId") {
      it("should create the step request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testSize = 0
        val testDepth = 1

        val mockStepRequest = mock[StepRequest]
        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        val actual = stepManager.createStepRequestWithId(
          expected.get,
          mockThreadReference,
          testSize,
          testDepth
        )
        actual should be(expected)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]
        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize,
          testDepth
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize,
          testDepth
        )
      }

      it("should remove any existing step requests for the thread if flag is true") {
        val stubStepRequest = stub[StepRequest]
        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepRequestWithId(
          TestRequestId,
          removeExistingRequests = true,
          mockThreadReference,
          testSize,
          testDepth
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepRequestWithId(
          TestRequestId,
          removeExistingRequests = true,
          mockThreadReference,
          testSize,
          testDepth
        )
      }
    }

    describe("#createStepRequest") {
      it("should create the step request and return Success(id)") {
        val expected = Success(TestRequestId)
        val testSize = 0
        val testDepth = 1

        val mockStepRequest = mock[StepRequest]
        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(mockStepRequest).once()

        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.setEnabled _).expects(true).once()

        val actual = stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the step request") {
        val expected = Failure(new Throwable)
        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .throwing(expected.failed.get).once()

        val actual = stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )
        actual should be (expected)
      }

      it("should remove any existing step requests for the thread if flag not provided") {
        val stubStepRequest = stub[StepRequest]
        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stubStepRequest).once()

        // First creation does not delete anything since nothing exists
        stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubStepRequest).once()

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(stub[StepRequest]).once()

        // Second request performs the actual deletion
        stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )
      }
    }

    describe("#hasStepRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stub[StepRequest]).once()

        stepManager.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize,
          testDepth
        )

        val actual = stepManager.hasStepRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = stepManager.hasStepRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasStepRequest") {
      it("should return true if it exists") {
        val expected = true

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stub[StepRequest]).once()

        stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )

        val actual = stepManager.hasStepRequest(mockThreadReference)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = stepManager.hasStepRequest(mockThreadReference)
        actual should be (expected)
      }
    }

    describe("#getStepRequestWithId") {
      it("should return Some(StepRequest) if found") {
        val expected = Some(stub[StepRequest])

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(expected.get).once()

        stepManager.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize,
          testDepth
        )

        val actual = stepManager.getStepRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = stepManager.getStepRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getStepRequestInfoWithId") {
      it("should return Some(StepInfo(class name, line number)) if the id exists") {
        val expected = Some(StepRequestInfo(TestRequestId, false, true, stub[ThreadReference], 0, 1))

        expected.foreach { case StepRequestInfo(_, _, _, t, s, d, _) =>
          (mockEventRequestManager.createStepRequest _).expects(t, s, d)
            .returning(stub[StepRequest]).once()

          stepManager.createStepRequestWithId(TestRequestId, t, s, d)
        }

        val actual = stepManager.getStepRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }

      it("should return None if there is no step with the id") {
        val expected = None

        val actual = stepManager.getStepRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#getStepRequest") {
      it("should return Some(Seq(StepRequest)) if found") {
        val expected = Seq(stub[StepRequest])

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(expected.head).once()

        stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )

        val actual = stepManager.getStepRequest(mockThreadReference).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = stepManager.getStepRequest(mockThreadReference)
        actual should be (expected)
      }
    }

    describe("#removeStepRequestWithId") {
      it("should return true if the step request was removed") {
        val expected = true
        val stubRequest = stub[StepRequest]

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stubRequest).once()

        stepManager.createStepRequestWithId(
          TestRequestId,
          mockThreadReference,
          testSize,
          testDepth
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = stepManager.removeStepRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the step request was not removed") {
        val expected = false

        val actual = stepManager.removeStepRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeStepRequest") {
      it("should return true if the step request was removed") {
        val expected = true
        val stubRequest = stub[StepRequest]

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .returning(stubRequest).once()

        stepManager.createStepRequest(
          mockThreadReference,
          testSize,
          testDepth
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = stepManager.removeStepRequest(mockThreadReference)
        actual should be (expected)
      }

      it("should return false if the step request was not removed") {
        val expected = false

        val actual = stepManager.removeStepRequest(mockThreadReference)
        actual should be (expected)
      }
    }
  }
}
