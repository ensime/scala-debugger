package org.scaladebugger.api.lowlevel.requests.filters.processors
import acyclic.file

import com.sun.jdi.ReferenceType
import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters.ClassReferenceFilter

class ClassReferenceFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val mockReferenceType = mock[ReferenceType]
  private val classReferenceFilter = ClassReferenceFilter(
    referenceType = mockReferenceType
  )
  private val classReferenceProcessor =
    new ClassReferenceFilterProcessor(classReferenceFilter)

  describe("ClassReferenceFilterProcessor") {
    describe("#process") {
      it("should add the class reference for access watchpoint requests") {
        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]

        (mockAccessWatchpointRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockAccessWatchpointRequest)
      }

      it("should add the class reference for class prepare requests") {
        val mockClassPrepareRequest = mock[ClassPrepareRequest]

        (mockClassPrepareRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockClassPrepareRequest)
      }

      it("should add the class reference for exception requests") {
        val mockExceptionRequest = mock[ExceptionRequest]

        (mockExceptionRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockExceptionRequest)
      }

      it("should add the class reference for method entry requests") {
        val mockMethodEntryRequest = mock[MethodEntryRequest]

        (mockMethodEntryRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockMethodEntryRequest)
      }

      it("should add the class reference for method exit requests") {
        val mockMethodExitRequest = mock[MethodExitRequest]

        (mockMethodExitRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockMethodExitRequest)
      }

      it("should add the class reference for modification watchpoint requests") {
        val mockModificationWatchpointRequest =
          mock[ModificationWatchpointRequest]

        (mockModificationWatchpointRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockModificationWatchpointRequest)
      }

      it("should add the class reference for monitor contended entered requests") {
        val mockMonitorContendedEnteredRequest =
          mock[MonitorContendedEnteredRequest]

        (mockMonitorContendedEnteredRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockMonitorContendedEnteredRequest)
      }

      it("should add the class reference for monitor contended enter requests") {
        val mockMonitorContendedEnterRequest =
          mock[MonitorContendedEnterRequest]

        (mockMonitorContendedEnterRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockMonitorContendedEnterRequest)
      }

      it("should add the class reference for monitor waited requests") {
        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]

        (mockMonitorWaitedRequest.addClassFilter(_: ReferenceType))
          .expects(mockReferenceType)

        classReferenceProcessor.process(mockMonitorWaitedRequest)
      }

      it("should add the class reference for monitor wait requests") {
        val mockMonitorWaitRequest = mock[MonitorWaitRequest]

        (mockMonitorWaitRequest.addClassFilter(_: ReferenceType)).expects(mockReferenceType)

        classReferenceProcessor.process(mockMonitorWaitRequest)
      }

      it("should add the class reference for step requests") {
        val mockStepRequest = mock[StepRequest]

        (mockStepRequest.addClassFilter(_: ReferenceType)).expects(mockReferenceType)

        classReferenceProcessor.process(mockStepRequest)
      }

      it("should not add the class reference for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addClassFilter(_: ReferenceType)).expects(mockReferenceType).never()

        classReferenceProcessor.process(mockEventRequest)
      }
    }
  }
}
