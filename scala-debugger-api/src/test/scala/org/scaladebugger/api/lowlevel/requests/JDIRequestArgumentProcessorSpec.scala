package org.scaladebugger.api.lowlevel.requests
import acyclic.file

import com.sun.jdi.request.EventRequest
import com.sun.jdi.{ObjectReference, ReferenceType, ThreadReference}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters._
import org.scaladebugger.api.lowlevel.requests.properties.{CustomProperty, EnabledProperty, SuspendPolicyProperty}

class JDIRequestArgumentProcessorSpec extends test.ParallelMockFunSpec
{
  // Create three mock filters to provide to the main filter processor
  private val mockArgumentsAndProcessors = Seq(
    newMockArgumentAndProcessor(),
    newMockArgumentAndProcessor(),
    newMockArgumentAndProcessor()
  )

  private val jdiRequestArgumentProcessor =
    new JDIRequestArgumentProcessor(mockArgumentsAndProcessors .map(_._1): _*)

  describe("JDIRequestArgumentProcessor") {
    describe("#process") {
      it("should execute every provided filter-to-processor on the request") {
        val mockEventRequest = mock[EventRequest]

        // Expect each processor to have its process method invoked once
        val mockProcessors = mockArgumentsAndProcessors.map(_._2)
        mockProcessors.foreach(mockProcessor =>
          (mockProcessor.process _).expects(mockEventRequest)
            .returning(mockEventRequest).once()
        )

        jdiRequestArgumentProcessor.process(mockEventRequest)
      }

      it("should return the updated request instance") {
        // Creating a stub request because we don't care what gets invoked
        // on the request itself
        val stubEventRequest = stub[EventRequest]

        val jdiRequestArgumentProcessor = new JDIRequestArgumentProcessor(
          ClassExclusionFilter(classPattern = ""),
          ClassInclusionFilter(classPattern = ""),
          ClassReferenceFilter(referenceType = mock[ReferenceType]),
          CountFilter(count = 3),
          InstanceFilter(objectReference = mock[ObjectReference]),
          SourceNameFilter(sourceNamePattern = ""),
          ThreadFilter(threadReference = mock[ThreadReference]),
          CustomProperty(key = "key", value = "value"),
          EnabledProperty(value = true),
          SuspendPolicyProperty.AllThreads
        )

        val result = jdiRequestArgumentProcessor.process(stubEventRequest)

        result should be (stubEventRequest)
      }
    }
  }

  /**
   * Creates a new filter (mock) that returns the same processor (mock)
   * every time.
   *
   * @return The tuple containing the filter and processor mocks
   */
  private def newMockArgumentAndProcessor(): (JDIRequestArgument, JDIRequestProcessor) = {
    val mockArgument = mock[JDIRequestArgument]
    val mockProcessor = mock[JDIRequestProcessor]

    // Allow any number of times for arbitrary usage of function
    (mockArgument.toProcessor _).expects()
      .returning(mockProcessor).anyNumberOfTimes()

    (mockArgument, mockProcessor)
  }
}
