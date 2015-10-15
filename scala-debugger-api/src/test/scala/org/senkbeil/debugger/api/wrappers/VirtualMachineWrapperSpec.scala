package org.senkbeil.debugger.api.wrappers

import com.sun.jdi.request._
import com.sun.jdi.VirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import test.JDIMockHelpers

class VirtualMachineWrapperSpec extends FunSpec with Matchers with MockFactory
  with JDIMockHelpers with OneInstancePerTest
{
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager)

  private val virtualMachineWrapper =
    new VirtualMachineWrapper(mockVirtualMachine)

  describe("VirtualMachineWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new VirtualMachineWrapper(null)
        }
      }
    }

    describe("#enableClassPrepareEvents") {
      it("should throw an exception if already enabled") {
        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stub[ClassPrepareRequest]).once()
        virtualMachineWrapper.enableClassPrepareEvents()

        intercept[AssertionError] {
          virtualMachineWrapper.enableClassPrepareEvents()
        }
      }

      it("should enable the event using the provided suspend policy") {
        val expected = 999 // Fake suspend policy

        val mockClassPrepareRequest = mock[ClassPrepareRequest]
        (mockClassPrepareRequest.enable _).expects().once()
        (mockClassPrepareRequest.setSuspendPolicy _).expects(expected).once()

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(mockClassPrepareRequest).once()

        virtualMachineWrapper.enableClassPrepareEvents(expected)
      }
    }

    describe("#disableClassPrepareEvents") {
      it("should throw an exception if not already enabled") {
        intercept[AssertionError] {
          virtualMachineWrapper.disableClassPrepareEvents()
        }
      }

      it("should delete the event request that was enabled earlier") {
        val stubClassPrepareRequest = stub[ClassPrepareRequest]

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stubClassPrepareRequest).once()
        virtualMachineWrapper.enableClassPrepareEvents()

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubClassPrepareRequest).once()
        virtualMachineWrapper.disableClassPrepareEvents()
      }
    }

    describe("#enableThreadStartEvents") {
      it("should throw an exception if already enabled") {
        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(stub[ThreadStartRequest]).once()
        virtualMachineWrapper.enableThreadStartEvents()

        intercept[AssertionError] {
          virtualMachineWrapper.enableThreadStartEvents()
        }
      }

      it("should enable the event using the provided suspend policy") {
        val expected = 999 // Fake suspend policy

        val mockThreadStartRequest = mock[ThreadStartRequest]
        (mockThreadStartRequest.enable _).expects().once()
        (mockThreadStartRequest.setSuspendPolicy _).expects(expected).once()

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(mockThreadStartRequest).once()

        virtualMachineWrapper.enableThreadStartEvents(expected)
      }
    }

    describe("#disableThreadStartEvents") {
      it("should throw an exception if not already enabled") {
        intercept[AssertionError] {
          virtualMachineWrapper.disableThreadStartEvents()
        }
      }

      it("should delete the event request that was enabled earlier") {
        val stubThreadStartRequest = stub[ThreadStartRequest]

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(stubThreadStartRequest).once()
        virtualMachineWrapper.enableThreadStartEvents()

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubThreadStartRequest).once()
        virtualMachineWrapper.disableThreadStartEvents()
      }
    }

    describe("#enableThreadDeathEvents") {
      it("should throw an exception if already enabled") {
        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(stub[ThreadDeathRequest]).once()
        virtualMachineWrapper.enableThreadDeathEvents()

        intercept[AssertionError] {
          virtualMachineWrapper.enableThreadDeathEvents()
        }
      }

      it("should enable the event using the provided suspend policy") {
        val expected = 999 // Fake suspend policy

        val mockThreadDeathRequest = mock[ThreadDeathRequest]
        (mockThreadDeathRequest.enable _).expects().once()
        (mockThreadDeathRequest.setSuspendPolicy _).expects(expected).once()

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(mockThreadDeathRequest).once()

        virtualMachineWrapper.enableThreadDeathEvents(expected)
      }
    }

    describe("#disableThreadDeathEvents") {
      it("should throw an exception if not already enabled") {
        intercept[AssertionError] {
          virtualMachineWrapper.disableThreadDeathEvents()
        }
      }

      it("should delete the event request that was enabled earlier") {
        val stubThreadDeathRequest = stub[ThreadDeathRequest]

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(stubThreadDeathRequest).once()
        virtualMachineWrapper.enableThreadDeathEvents()

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubThreadDeathRequest).once()
        virtualMachineWrapper.disableThreadDeathEvents()
      }
    }

    describe("#enableExceptionEvents") {
      it("should throw an exception if already enabled") {
        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *).returning(stub[ExceptionRequest]).once()
        virtualMachineWrapper.enableExceptionEvents()

        intercept[AssertionError] {
          virtualMachineWrapper.enableExceptionEvents()
        }
      }

      it("should enable the event using the provided suspend policy") {
        val expected = 999 // Fake suspend policy

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockExceptionRequest.enable _).expects().once()
        (mockExceptionRequest.setSuspendPolicy _).expects(expected).once()

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, false, true).returning(mockExceptionRequest).once()

        virtualMachineWrapper.enableExceptionEvents(expected)
      }
    }

    describe("#disableExceptionEvents") {
      it("should throw an exception if not already enabled") {
        intercept[AssertionError] {
          virtualMachineWrapper.disableExceptionEvents()
        }
      }

      it("should delete the event request that was enabled earlier") {
        val stubExceptionRequest = stub[ExceptionRequest]

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *).returning(stubExceptionRequest).once()
        virtualMachineWrapper.enableExceptionEvents()

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubExceptionRequest).once()
        virtualMachineWrapper.disableExceptionEvents()
      }
    }
  }
}
