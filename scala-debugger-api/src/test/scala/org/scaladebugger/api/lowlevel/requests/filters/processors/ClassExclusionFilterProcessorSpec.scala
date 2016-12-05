package org.scaladebugger.api.lowlevel.requests.filters.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters.ClassExclusionFilter

class ClassExclusionFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testPattern = "some pattern"
  private val classExclusionFilter = ClassExclusionFilter(
    classPattern = testPattern
  )
  private val classExclusionProcessor =
    new ClassExclusionFilterProcessor(classExclusionFilter)

  describe("ClassExclusionFilterProcessor") {
    describe("#process") {
      it("should add the class exclusion pattern for access watchpoint requests") {
        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]

        (mockAccessWatchpointRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockAccessWatchpointRequest)
      }

      it("should add the class exclusion pattern for class prepare requests") {
        val mockClassPrepareRequest = mock[ClassPrepareRequest]

        (mockClassPrepareRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockClassPrepareRequest)
      }

      it("should add the class exclusion pattern for class unload requests") {
        val mockClassUnloadRequest = mock[ClassUnloadRequest]

        (mockClassUnloadRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockClassUnloadRequest)
      }

      it("should add the class exclusion pattern for exception requests") {
        val mockExceptionRequest = mock[ExceptionRequest]

        (mockExceptionRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockExceptionRequest)
      }

      it("should add the class exclusion pattern for method entry requests") {
        val mockMethodEntryRequest = mock[MethodEntryRequest]

        (mockMethodEntryRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockMethodEntryRequest)
      }

      it("should add the class exclusion pattern for method exit requests") {
        val mockMethodExitRequest = mock[MethodExitRequest]

        (mockMethodExitRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockMethodExitRequest)
      }

      it("should add the class exclusion pattern for modification watchpoint requests") {
        val mockModificationWatchpointRequest =
          mock[ModificationWatchpointRequest]

        (mockModificationWatchpointRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockModificationWatchpointRequest)
      }

      it("should add the class exclusion pattern for monitor contended entered requests") {
        val mockMonitorContendedEnteredRequest =
          mock[MonitorContendedEnteredRequest]

        (mockMonitorContendedEnteredRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockMonitorContendedEnteredRequest)
      }

      it("should add the class exclusion pattern for monitor contended enter requests") {
        val mockMonitorContendedEnterRequest =
          mock[MonitorContendedEnterRequest]

        (mockMonitorContendedEnterRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockMonitorContendedEnterRequest)
      }

      it("should add the class exclusion pattern for monitor waited requests") {
        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]

        (mockMonitorWaitedRequest.addClassExclusionFilter _)
          .expects(testPattern)

        classExclusionProcessor.process(mockMonitorWaitedRequest)
      }

      it("should add the class exclusion pattern for monitor wait requests") {
        val mockMonitorWaitRequest = mock[MonitorWaitRequest]

        (mockMonitorWaitRequest.addClassExclusionFilter _).expects(testPattern)

        classExclusionProcessor.process(mockMonitorWaitRequest)
      }

      it("should add the class exclusion pattern for step requests") {
        val mockStepRequest = mock[StepRequest]

        (mockStepRequest.addClassExclusionFilter _).expects(testPattern)

        classExclusionProcessor.process(mockStepRequest)
      }

      it("should not add the class exclusion pattern for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addClassExclusionFilter _).expects(testPattern).never()

        classExclusionProcessor.process(mockEventRequest)
      }
    }
  }
}
