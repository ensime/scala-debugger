package org.senkbeil.debugger.jdi.requests.properties

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class SuspendPolicyPropertySpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testPolicy = 0
  private val suspendPolicyProperty = SuspendPolicyProperty(policy = testPolicy)

  describe("SuspendPolicyProperty") {
    describe("#toProcessor") {
      it("should return a processor containing the suspend policy property") {
        suspendPolicyProperty.toProcessor.argument should
          be (suspendPolicyProperty)
      }
    }
  }
}
