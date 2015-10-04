package org.senkbeil.debugger.jdi.requests

import com.sun.jdi.{ThreadReference, ObjectReference, ReferenceType}
import com.sun.jdi.request.EventRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.requests.filters._
import org.senkbeil.debugger.jdi.requests.processors.JDIRequestProcessor

class JDIRequestFilterProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  // Create three mock filters to provide to the main filter processor
  private val mockFiltersAndProcessors = Seq(
    newMockFilterAndProcessor(),
    newMockFilterAndProcessor(),
    newMockFilterAndProcessor()
  )

  private val jdiRequestFilterProcessor =
    new JDIRequestFilterProcessor(mockFiltersAndProcessors .map(_._1): _*)

  describe("JDIRequestFilterProcessor") {
    describe("#process") {
      it("should execute every provided filter-to-processor on the request") {
        val mockEventRequest = mock[EventRequest]

        // Expect each processor to have its process method invoked once
        val mockProcessors = mockFiltersAndProcessors.map(_._2)
        mockProcessors.foreach(mockProcessor =>
          (mockProcessor.process _).expects(mockEventRequest)
            .returning(mockEventRequest).once()
        )

        jdiRequestFilterProcessor.process(mockEventRequest)
      }

      it("should return the updated request instance") {
        // Creating a stub request because we don't care what gets invoked
        // on the request itself
        val stubEventRequest = stub[EventRequest]

        val jdiRequestFilterProcessor = new JDIRequestFilterProcessor(
          ClassExclusionFilter(classPattern = ""),
          ClassInclusionFilter(classPattern = ""),
          ClassReferenceFilter(referenceType = mock[ReferenceType]),
          CountFilter(count = 3),
          InstanceFilter(objectReference = mock[ObjectReference]),
          SourceNameFilter(sourceNamePattern = ""),
          ThreadFilter(threadReference = mock[ThreadReference])
        )

        val result = jdiRequestFilterProcessor.process(stubEventRequest)

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
  private def newMockFilterAndProcessor(): (JDIRequestFilter, JDIRequestProcessor) = {
    val mockFilter = mock[JDIRequestFilter]
    val mockProcessor = mock[JDIRequestProcessor]

    // Allow any number of times for arbitrary usage of function
    (mockFilter.toProcessor _).expects()
      .returning(mockProcessor).anyNumberOfTimes()

    (mockFilter, mockProcessor)
  }
}
