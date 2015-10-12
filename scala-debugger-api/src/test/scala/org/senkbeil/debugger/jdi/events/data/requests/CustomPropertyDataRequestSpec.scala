package org.senkbeil.debugger.jdi.events.data.requests

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class CustomPropertyDataRequestSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testKey = "some key"
  private val customPropertyDataRequest =
    CustomPropertyDataRequest(key = testKey)

  describe("CustomPropertyDataRequest") {
    describe("#toProcessor") {
      it("should return a processor containing the custom property data request") {
        customPropertyDataRequest.toProcessor.argument should
          be (customPropertyDataRequest)
      }
    }
  }
}
