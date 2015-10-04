package org.senkbeil.debugger.jdi.requests.processors

import com.sun.jdi.ObjectReference
import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.requests.filters.InstanceFilter

class InstanceProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockObjectReference = mock[ObjectReference]
  private val instanceFilter = InstanceFilter(
    objectReference = mockObjectReference
  )
  private val instanceProcessor = new InstanceProcessor(instanceFilter)

  describe("InstanceProcessor") {
    describe("#process") {
      it("should add the instance for access watchpoint requests") {
        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]

        (mockAccessWatchpointRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockAccessWatchpointRequest)
      }

      it("should add the instance for breakpoint requests") {
        val mockBreakpointRequest = mock[BreakpointRequest]

        (mockBreakpointRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockBreakpointRequest)
      }

      it("should add the instance for exception requests") {
        val mockExceptionRequest = mock[ExceptionRequest]

        (mockExceptionRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockExceptionRequest)
      }

      it("should add the instance for method entry requests") {
        val mockMethodEntryRequest = mock[MethodEntryRequest]

        (mockMethodEntryRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockMethodEntryRequest)
      }

      it("should add the instance for method exit requests") {
        val mockMethodExitRequest = mock[MethodExitRequest]

        (mockMethodExitRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockMethodExitRequest)
      }

      it("should add the instance for modification watchpoint requests") {
        val mockModificationWatchpointRequest =
          mock[ModificationWatchpointRequest]

        (mockModificationWatchpointRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockModificationWatchpointRequest)
      }

      it("should add the instance for monitor contended entered requests") {
        val mockMonitorContendedEnteredRequest =
          mock[MonitorContendedEnteredRequest]

        (mockMonitorContendedEnteredRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockMonitorContendedEnteredRequest)
      }

      it("should add the instance for monitor contended enter requests") {
        val mockMonitorContendedEnterRequest =
          mock[MonitorContendedEnterRequest]

        (mockMonitorContendedEnterRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockMonitorContendedEnterRequest)
      }

      it("should add the instance for monitor waited requests") {
        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]

        (mockMonitorWaitedRequest.addInstanceFilter _)
          .expects(mockObjectReference)

        instanceProcessor.process(mockMonitorWaitedRequest)
      }

      it("should add the instance for monitor wait requests") {
        val mockMonitorWaitRequest = mock[MonitorWaitRequest]

        (mockMonitorWaitRequest.addInstanceFilter _).expects(mockObjectReference)

        instanceProcessor.process(mockMonitorWaitRequest)
      }

      it("should add the instance for step requests") {
        val mockStepRequest = mock[StepRequest]

        (mockStepRequest.addInstanceFilter _).expects(mockObjectReference)

        instanceProcessor.process(mockStepRequest)
      }

      it("should not add the instance for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addInstanceFilter _).expects(mockObjectReference).never()

        instanceProcessor.process(mockEventRequest)
      }
    }
  }
}
