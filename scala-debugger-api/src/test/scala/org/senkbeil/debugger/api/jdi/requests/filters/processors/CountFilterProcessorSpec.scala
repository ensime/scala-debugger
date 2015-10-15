package org.senkbeil.debugger.api.jdi.requests.filters.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.jdi.requests.filters.CountFilter

class CountFilterProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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
