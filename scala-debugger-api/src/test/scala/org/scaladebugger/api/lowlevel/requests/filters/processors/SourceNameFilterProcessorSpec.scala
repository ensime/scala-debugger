package org.scaladebugger.api.lowlevel.requests.filters.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters.SourceNameFilter

class SourceNameFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testSourceNamePattern = "some pattern"
  private val sourceNameFilter = SourceNameFilter(
    sourceNamePattern = testSourceNamePattern
  )
  private val sourceNameProcessor =
    new SourceNameFilterProcessor(sourceNameFilter)

  describe("SourceNameFilterProcessor") {
    describe("#process") {
      it("should add the source name for class prepare requests") {
        val mockClassPrepareRequest = mock[ClassPrepareRequest]

        (mockClassPrepareRequest.addSourceNameFilter _)
          .expects(testSourceNamePattern)

        sourceNameProcessor.process(mockClassPrepareRequest)
      }

      it("should not add the source name for any other request") {
        val mockEventRequest = mock[EventRequest]

        // TODO: Since there is no method for this generic event, what do we
        //       really test here?
        //(mockEventRequest.addSourceNameFilter _).expects(mockSourceNameReference).never()

        sourceNameProcessor.process(mockEventRequest)
      }
    }
  }
}
