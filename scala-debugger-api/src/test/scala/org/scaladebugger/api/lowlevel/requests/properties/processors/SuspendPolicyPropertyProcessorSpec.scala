package org.scaladebugger.api.lowlevel.requests.properties.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.properties.SuspendPolicyProperty

class SuspendPolicyPropertyProcessorSpec extends test.ParallelMockFunSpec
{
  private val testPolicy = 0
  private val suspendPolicyProperty = SuspendPolicyProperty(policy = testPolicy)
  private val suspendPolicyProcessor =
    new SuspendPolicyPropertyProcessor(suspendPolicyProperty)

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
