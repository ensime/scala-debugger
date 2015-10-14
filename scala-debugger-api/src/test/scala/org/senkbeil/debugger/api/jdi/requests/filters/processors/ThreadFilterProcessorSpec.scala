package org.senkbeil.debugger.api.jdi.requests.filters.processors

import com.sun.jdi.ThreadReference
import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.jdi.requests.filters.ThreadFilter

class ThreadFilterProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockThreadReference = mock[ThreadReference]
  private val threadFilter = ThreadFilter(
    threadReference = mockThreadReference
  )
  private val threadProcessor = new ThreadFilterProcessor(threadFilter)

  describe("ThreadFilterProcessor") {
    describe("#process") {
      it("should add the thread for access watchpoint requests") {
        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]

        (mockAccessWatchpointRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockAccessWatchpointRequest)
      }

      it("should add the thread for breakpoint requests") {
        val mockBreakpointRequest = mock[BreakpointRequest]

        (mockBreakpointRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockBreakpointRequest)
      }

      it("should add the thread for exception requests") {
        val mockExceptionRequest = mock[ExceptionRequest]

        (mockExceptionRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockExceptionRequest)
      }

      it("should add the thread for method entry requests") {
        val mockMethodEntryRequest = mock[MethodEntryRequest]

        (mockMethodEntryRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockMethodEntryRequest)
      }

      it("should add the thread for method exit requests") {
        val mockMethodExitRequest = mock[MethodExitRequest]

        (mockMethodExitRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockMethodExitRequest)
      }

      it("should add the thread for modification watchpoint requests") {
        val mockModificationWatchpointRequest =
          mock[ModificationWatchpointRequest]

        (mockModificationWatchpointRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockModificationWatchpointRequest)
      }

      it("should add the thread for monitor contended entered requests") {
        val mockMonitorContendedEnteredRequest =
          mock[MonitorContendedEnteredRequest]

        (mockMonitorContendedEnteredRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockMonitorContendedEnteredRequest)
      }

      it("should add the thread for monitor contended enter requests") {
        val mockMonitorContendedEnterRequest =
          mock[MonitorContendedEnterRequest]

        (mockMonitorContendedEnterRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockMonitorContendedEnterRequest)
      }

      it("should add the thread for monitor waited requests") {
        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]

        (mockMonitorWaitedRequest.addThreadFilter _)
          .expects(mockThreadReference)

        threadProcessor.process(mockMonitorWaitedRequest)
      }

      it("should add the thread for monitor wait requests") {
        val mockMonitorWaitRequest = mock[MonitorWaitRequest]

        (mockMonitorWaitRequest.addThreadFilter _).expects(mockThreadReference)

        threadProcessor.process(mockMonitorWaitRequest)
      }

      it("should add the thread for thread death requests") {
        val mockThreadDeathRequest = mock[ThreadDeathRequest]

        (mockThreadDeathRequest.addThreadFilter _).expects(mockThreadReference)

        threadProcessor.process(mockThreadDeathRequest)
      }

      it("should add the thread for thread start requests") {
        val mockThreadStartRequest = mock[ThreadStartRequest]

        (mockThreadStartRequest.addThreadFilter _).expects(mockThreadReference)

        threadProcessor.process(mockThreadStartRequest)
      }

      it("should not add the thread for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addThreadFilter _).expects(mockThreadReference).never()

        threadProcessor.process(mockEventRequest)
      }
    }
  }
}
