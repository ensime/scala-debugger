package org.senkbeil.debugger.api.jdi.requests.properties

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class CustomPropertySpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockKey = mock[AnyRef]
  private val mockValue = mock[AnyRef]
  private val customProperty = CustomProperty(key = mockKey, value = mockValue)

  describe("CustomProperty") {
    describe("#toProcessor") {
      it("should return a processor containing the custom property") {
        customProperty.toProcessor.argument should be (customProperty)
      }
    }
  }
}
