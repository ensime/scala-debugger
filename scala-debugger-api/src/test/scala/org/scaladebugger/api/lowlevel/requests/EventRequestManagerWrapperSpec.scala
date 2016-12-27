package org.scaladebugger.api.lowlevel.requests

import com.sun.jdi.{Field, Location, ReferenceType, ThreadReference}
import com.sun.jdi.request._
import org.scaladebugger.api.lowlevel.requests.properties.{CustomProperty, EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class EventRequestManagerWrapperSpec extends ParallelMockFunSpec {
  private val mockEventRequestManager = mock[EventRequestManager]
  private val eventRequestManagerWrapper =
    new EventRequestManagerWrapper(mockEventRequestManager)

  describe("RequestManager") {
    describe("#createAccessWatchpointRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        val mockField = mock[Field]

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField).once()

        eventRequestManagerWrapper.createAccessWatchpointRequest(mockField)
      }

      it("should apply extra arguments to the event request if provided") {
        val mockField = mock[Field]
        val mockRequest = mock[AccessWatchpointRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createAccessWatchpointRequest _)
            .expects(mockField).returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper
          .createAccessWatchpointRequest(mockField, arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockField = mock[Field]
        val mockRequest = mock[AccessWatchpointRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createAccessWatchpointRequest _)
            .expects(mockField).returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper
          .createAccessWatchpointRequest(mockField, arguments: _*)
      }
    }

    describe("#createBreakpointRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        val mockLocation = mock[Location]

        (mockEventRequestManager.createBreakpointRequest _)
          .expects(mockLocation).once()

        eventRequestManagerWrapper.createBreakpointRequest(mockLocation)
      }

      it("should apply extra arguments to the event request if provided") {
        val mockLocation = mock[Location]
        val mockRequest = mock[BreakpointRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createBreakpointRequest _)
            .expects(mockLocation).returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper
          .createBreakpointRequest(mockLocation, arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockLocation = mock[Location]
        val mockRequest = mock[BreakpointRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createBreakpointRequest _)
            .expects(mockLocation).returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper
          .createBreakpointRequest(mockLocation, arguments: _*)
      }
    }

    describe("#createClassPrepareRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createClassPrepareRequest _).expects().once()

        eventRequestManagerWrapper.createClassPrepareRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[ClassPrepareRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createClassPrepareRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createClassPrepareRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[ClassPrepareRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createClassPrepareRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createClassPrepareRequest(arguments: _*)
      }
    }

    describe("#createClassUnloadRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createClassUnloadRequest _).expects().once()

        eventRequestManagerWrapper.createClassUnloadRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[ClassUnloadRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createClassUnloadRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createClassUnloadRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[ClassUnloadRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createClassUnloadRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createClassUnloadRequest(arguments: _*)
      }
    }

    describe("#createExceptionRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        val mockReferenceType = mock[ReferenceType]
        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .once()

        eventRequestManagerWrapper.createExceptionRequest(
          mockReferenceType, testNotifyCaught, testNotifyUncaught
        )
      }

      it("should apply extra arguments to the event request if provided") {
        val mockReferenceType = mock[ReferenceType]
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockRequest = mock[ExceptionRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createExceptionRequest(
          mockReferenceType, testNotifyCaught, testNotifyUncaught,
          arguments: _*
        )
      }

      it("should apply only the last enabled property if provided") {
        val mockReferenceType = mock[ReferenceType]
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockRequest = mock[ExceptionRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createExceptionRequest(
          mockReferenceType, testNotifyCaught, testNotifyUncaught,
          arguments: _*
        )
      }
    }

    describe("#createMethodEntryRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMethodEntryRequest _).expects().once()

        eventRequestManagerWrapper.createMethodEntryRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MethodEntryRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMethodEntryRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createMethodEntryRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MethodEntryRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMethodEntryRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createMethodEntryRequest(arguments: _*)
      }
    }

    describe("#createMethodExitRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMethodExitRequest _).expects().once()

        eventRequestManagerWrapper.createMethodExitRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MethodExitRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMethodExitRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createMethodExitRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MethodExitRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMethodExitRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createMethodExitRequest(arguments: _*)
      }
    }

    describe("#createModificationWatchpointRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        val mockField = mock[Field]

        (mockEventRequestManager.createModificationWatchpointRequest _)
          .expects(mockField).once()

        eventRequestManagerWrapper.createModificationWatchpointRequest(mockField)
      }

      it("should apply extra arguments to the event request if provided") {
        val mockField = mock[Field]
        val mockRequest = mock[ModificationWatchpointRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createModificationWatchpointRequest _)
            .expects(mockField).returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper
          .createModificationWatchpointRequest(mockField, arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockField = mock[Field]
        val mockRequest = mock[ModificationWatchpointRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createModificationWatchpointRequest _)
            .expects(mockField).returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper
          .createModificationWatchpointRequest(mockField, arguments: _*)
      }
    }

    describe("#createMonitorContendedEnteredRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMonitorContendedEnteredRequest _)
          .expects().once()

        eventRequestManagerWrapper.createMonitorContendedEnteredRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MonitorContendedEnteredRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMonitorContendedEnteredRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper
          .createMonitorContendedEnteredRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MonitorContendedEnteredRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMonitorContendedEnteredRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper
          .createMonitorContendedEnteredRequest(arguments: _*)
      }
    }

    describe("#createMonitorContendedEnterRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMonitorContendedEnterRequest _)
          .expects().once()

        eventRequestManagerWrapper.createMonitorContendedEnterRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MonitorContendedEnterRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMonitorContendedEnterRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper
          .createMonitorContendedEnterRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MonitorContendedEnterRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMonitorContendedEnterRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper
          .createMonitorContendedEnterRequest(arguments: _*)
      }
    }

    describe("#createMonitorWaitedRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMonitorWaitedRequest _).expects().once()

        eventRequestManagerWrapper.createMonitorWaitedRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MonitorWaitedRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMonitorWaitedRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createMonitorWaitedRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MonitorWaitedRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMonitorWaitedRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createMonitorWaitedRequest(arguments: _*)
      }
    }

    describe("#createMonitorWaitRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createMonitorWaitRequest _).expects().once()

        eventRequestManagerWrapper.createMonitorWaitRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[MonitorWaitRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createMonitorWaitRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createMonitorWaitRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[MonitorWaitRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createMonitorWaitRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createMonitorWaitRequest(arguments: _*)
      }
    }

    describe("#createStepRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createStepRequest _)
          .expects(mockThreadReference, testSize, testDepth)
          .once()

        eventRequestManagerWrapper.createStepRequest(
          mockThreadReference, testSize, testDepth
        )
      }

      it("should apply extra arguments to the event request if provided") {
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        val mockRequest = mock[StepRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createStepRequest _)
            .expects(mockThreadReference, testSize, testDepth)
            .returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createStepRequest(
          mockThreadReference, testSize, testDepth,
          arguments: _*
        )
      }

      it("should apply only the last enabled property if provided") {
        val mockThreadReference = mock[ThreadReference]
        val testSize = 0
        val testDepth = 1

        val mockRequest = mock[StepRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createStepRequest _)
            .expects(mockThreadReference, testSize, testDepth)
            .returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createStepRequest(
          mockThreadReference, testSize, testDepth,
          arguments: _*
        )
      }
    }

    describe("#createThreadDeathRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createThreadDeathRequest _).expects().once()

        eventRequestManagerWrapper.createThreadDeathRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[ThreadDeathRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createThreadDeathRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createThreadDeathRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[ThreadDeathRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createThreadDeathRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createThreadDeathRequest(arguments: _*)
      }
    }

    describe("#createThreadStartRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createThreadStartRequest _).expects().once()

        eventRequestManagerWrapper.createThreadStartRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[ThreadStartRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createThreadStartRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createThreadStartRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[ThreadStartRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createThreadStartRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createThreadStartRequest(arguments: _*)
      }
    }

    describe("#createVMDeathRequest") {
      it("should just invoke the underlying method if no extra arguments") {
        (mockEventRequestManager.createVMDeathRequest _).expects().once()

        eventRequestManagerWrapper.createVMDeathRequest()
      }

      it("should apply extra arguments to the event request if provided") {
        val mockRequest = mock[VMDeathRequest]
        val arguments = Seq(
          SuspendPolicyProperty(policy = 1),
          CustomProperty(key = "key", value = "value")
        )

        inSequence {
          (mockEventRequestManager.createVMDeathRequest _)
            .expects().returning(mockRequest).once()

          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.putProperty _).expects("key", "value").once()
        }

        eventRequestManagerWrapper.createVMDeathRequest(arguments: _*)
      }

      it("should apply only the last enabled property if provided") {
        val mockRequest = mock[VMDeathRequest]
        val arguments = Seq(
          EnabledProperty(value = false),
          SuspendPolicyProperty(policy = 1),
          EnabledProperty(value = true),
          SuspendPolicyProperty(policy = 0)
        )

        inSequence {
          (mockEventRequestManager.createVMDeathRequest _)
            .expects().returning(mockRequest).once()

          // Enabled should always be provided last and only once
          (mockRequest.setSuspendPolicy _).expects(1).once()
          (mockRequest.setSuspendPolicy _).expects(0).once()
          (mockRequest.setEnabled _).expects(true).once()
        }

        eventRequestManagerWrapper.createVMDeathRequest(arguments: _*)
      }
    }
  }
}
