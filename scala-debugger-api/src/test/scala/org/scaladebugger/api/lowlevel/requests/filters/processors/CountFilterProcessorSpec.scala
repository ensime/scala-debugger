package org.scaladebugger.api.lowlevel.requests.filters.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.filters.CountFilter

class CountFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testCount = 3
  private val countFilter = CountFilter(count = testCount)
  private val countProcessor = new CountFilterProcessor(countFilter)

  describe("CountFilterProcessor") {
    describe("#process") {
      it("should add the count for all requests") {
        val mockEventRequest = mock[EventRequest]

        (mockEventRequest.addCountFilter _).expects(testCount)

        countProcessor.process(mockEventRequest)
      }
    }
  }
}
