package org.senkbeil.debugger.jdi.requests.properties

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class EnabledPropertySpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testValue = false
  private val enabledProperty = EnabledProperty(value = testValue)

  describe("EnabledProperty") {
    describe("#toProcessor") {
      it("should return a processor containing the enabled property") {
        enabledProperty.toProcessor.argument should be (enabledProperty)
      }
    }
  }
}
