package org.senkbeil.debugger.api.jdi.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class MinTriggerFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testCount = 3
  private val minTriggerFilter = MinTriggerFilter(count = testCount)

  describe("MinTriggerFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the min trigger filter") {
        minTriggerFilter.toProcessor.argument should be (minTriggerFilter)
      }
    }
  }
}
