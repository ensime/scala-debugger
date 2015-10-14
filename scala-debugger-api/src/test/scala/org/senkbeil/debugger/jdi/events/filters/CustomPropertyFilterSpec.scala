package org.senkbeil.debugger.jdi.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class CustomPropertyFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testKey = "some key"
  private val testValue = "some value"
  private val customPropertyFilter = CustomPropertyFilter(
    key = testKey,
    value = testValue
  )

  describe("CustomPropertyFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the custom property filter") {
        customPropertyFilter.toProcessor.argument should
          be (customPropertyFilter)
      }
    }
  }
}
