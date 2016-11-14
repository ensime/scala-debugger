package org.scaladebugger.api.lowlevel.requests.filters.processors
import acyclic.file

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters.ClassInclusionFilter

class ClassInclusionFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testPattern = "some pattern"
  private val classInclusionFilter = ClassInclusionFilter(
    classPattern = testPattern
  )
  private val classInclusionProcessor =
    new ClassInclusionFilterProcessor(classInclusionFilter)

  describe("ClassInclusionFilterProcessor") {
    describe("#process") {
      it("should add the class inclusion pattern for access watchpoint requests") {
        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]

        (mockAccessWatchpointRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockAccessWatchpointRequest)
      }

      it("should add the class inclusion pattern for class prepare requests") {
        val mockClassPrepareRequest = mock[ClassPrepareRequest]

        (mockClassPrepareRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockClassPrepareRequest)
      }

      it("should add the class inclusion pattern for class unload requests") {
        val mockClassUnloadRequest = mock[ClassUnloadRequest]

        (mockClassUnloadRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockClassUnloadRequest)
      }

      it("should add the class inclusion pattern for exception requests") {
        val mockExceptionRequest = mock[ExceptionRequest]

        (mockExceptionRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockExceptionRequest)
      }

      it("should add the class inclusion pattern for method entry requests") {
        val mockMethodEntryRequest = mock[MethodEntryRequest]

        (mockMethodEntryRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockMethodEntryRequest)
      }

      it("should add the class inclusion pattern for method exit requests") {
        val mockMethodExitRequest = mock[MethodExitRequest]

        (mockMethodExitRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockMethodExitRequest)
      }

      it("should add the class inclusion pattern for modification watchpoint requests") {
        val mockModificationWatchpointRequest =
          mock[ModificationWatchpointRequest]

        (mockModificationWatchpointRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockModificationWatchpointRequest)
      }

      it("should add the class inclusion pattern for monitor contended entered requests") {
        val mockMonitorContendedEnteredRequest =
          mock[MonitorContendedEnteredRequest]

        (mockMonitorContendedEnteredRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockMonitorContendedEnteredRequest)
      }

      it("should add the class inclusion pattern for monitor contended enter requests") {
        val mockMonitorContendedEnterRequest =
          mock[MonitorContendedEnterRequest]

        (mockMonitorContendedEnterRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockMonitorContendedEnterRequest)
      }

      it("should add the class inclusion pattern for monitor waited requests") {
        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]

        (mockMonitorWaitedRequest.addClassFilter(_: String))
          .expects(testPattern)

        classInclusionProcessor.process(mockMonitorWaitedRequest)
      }

      it("should add the class inclusion pattern for monitor wait requests") {
        val mockMonitorWaitRequest = mock[MonitorWaitRequest]

        (mockMonitorWaitRequest.addClassFilter(_: String)).expects(testPattern)

        classInclusionProcessor.process(mockMonitorWaitRequest)
      }

      it("should add the class inclusion pattern for step requests") {
        val mockStepRequest = mock[StepRequest]

        (mockStepRequest.addClassFilter(_: String)).expects(testPattern)

        classInclusionProcessor.process(mockStepRequest)
      }

      it("should not add the class inclusion pattern for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addClassFilter(_: String)).expects(testPattern).never()

        classInclusionProcessor.process(mockEventRequest)
      }
    }
  }
}
