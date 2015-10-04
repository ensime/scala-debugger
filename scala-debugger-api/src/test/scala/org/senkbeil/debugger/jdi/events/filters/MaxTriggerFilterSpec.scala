package org.senkbeil.debugger.jdi.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

class MaxTriggerFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testCount = 3
  private val maxTriggerFilter = MaxTriggerFilter(count = testCount)

  describe("MaxTriggerFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the max trigger filter") {
        maxTriggerFilter.toProcessor.filter should be (maxTriggerFilter)
      }
    }
  }
}
