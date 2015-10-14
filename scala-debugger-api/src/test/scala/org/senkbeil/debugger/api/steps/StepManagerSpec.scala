package org.senkbeil.debugger.api.steps

import com.sun.jdi.{ThreadReference, VirtualMachine}
import com.sun.jdi.request.{EventRequest, StepRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class StepManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager).once()

  private val stepManager = new StepManager(mockVirtualMachine)

  private def expectDeleteStepRequests() = {
    val mockStepRequests = Seq(mock[StepRequest]).asJava

    (mockEventRequestManager.stepRequests _).expects()
      .returning(mockStepRequests).once()
    (mockEventRequestManager.deleteEventRequests _)
      .expects(mockStepRequests).once()
  }

  describe("StepManager") {
    describe("#stepOver") {
      it("should remove any existing step requests") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OVER
        ).returning(stub[StepRequest]).once()

        stepManager.stepOver(mockThreadReference)
      }

      it("should send a request to step over the current line of execution") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OVER
        ).returning(stub[StepRequest]).once()

        stepManager.stepOver(mockThreadReference)
      }

      it("should set the suspend policy to the thread only") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOver(stub[ThreadReference])
      }

      it("should add a count filter of 1 to the new step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOver(stub[ThreadReference])
      }

      it("should enable the step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOver(stub[ThreadReference])
      }
    }

    describe("#stepInto") {
      it("should remove any existing step requests") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_INTO
        ).returning(stub[StepRequest]).once()

        stepManager.stepInto(mockThreadReference)
      }

      it("should send a request to step into the current line of execution") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_INTO
        ).returning(stub[StepRequest]).once()

        stepManager.stepInto(mockThreadReference)
      }

      it("should set the suspend policy to the thread only") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepInto(stub[ThreadReference])
      }

      it("should add a count filter of 1 to the new step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepInto(stub[ThreadReference])
      }

      it("should enable the step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepInto(stub[ThreadReference])
      }
    }

    describe("#stepOut") {
      it("should remove any existing step requests") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OUT
        ).returning(stub[StepRequest]).once()

        stepManager.stepOut(mockThreadReference)
      }

      it("should send a request to step out of the current line of execution") {
        expectDeleteStepRequests()
        val mockThreadReference = mock[ThreadReference]

        (mockEventRequestManager.createStepRequest _).expects(
          mockThreadReference,
          StepRequest.STEP_LINE,
          StepRequest.STEP_OUT
        ).returning(stub[StepRequest]).once()

        stepManager.stepOut(mockThreadReference)
      }

      it("should set the suspend policy to the thread only") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOut(stub[ThreadReference])
      }

      it("should add a count filter of 1 to the new step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(1).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOut(stub[ThreadReference])
      }

      it("should enable the step request") {
        expectDeleteStepRequests()
        val mockStepRequest = mock[StepRequest]

        (mockEventRequestManager.createStepRequest _).expects(*, *, *)
          .returning(mockStepRequest).once()

        (mockStepRequest.setSuspendPolicy _).expects(*).once()
        (mockStepRequest.addCountFilter _).expects(*).once()
        (mockStepRequest.enable _).expects().once()

        stepManager.stepOut(stub[ThreadReference])
      }
    }
  }
}
