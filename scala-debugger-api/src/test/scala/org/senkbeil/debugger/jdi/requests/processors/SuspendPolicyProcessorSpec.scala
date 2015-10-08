package org.senkbeil.debugger.jdi.requests.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.requests.properties.SuspendPolicyProperty

class SuspendPolicyProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testPolicy = 0
  private val suspendPolicyProperty = SuspendPolicyProperty(policy = testPolicy)
  private val suspendPolicyProcessor =
    new SuspendPolicyProcessor(suspendPolicyProperty)

  describe("SuspendPolicyPropertyProcessor") {
    describe("#process") {
      it("should set the suspend policy of the event request") {
        val mockEventRequest = mock[EventRequest]

        (mockEventRequest.setSuspendPolicy _).expects(testPolicy).once()

        suspendPolicyProcessor.process(mockEventRequest)
      }
    }
  }
}
