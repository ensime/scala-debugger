package org.senkbeil.debugger.api.jdi.requests.properties.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.jdi.requests.properties.EnabledProperty

class EnabledPropertyProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testValue = false
  private val enabledProperty = EnabledProperty(value = testValue)
  private val enabledProcessor = new EnabledPropertyProcessor(enabledProperty)

  describe("EnabledPropertyProcessor") {
    describe("#process") {
      it("should set the enabled status of the event request") {
        val mockEventRequest = mock[EventRequest]

        (mockEventRequest.setEnabled _).expects(testValue).once()

        enabledProcessor.process(mockEventRequest)
      }
    }
  }
}
