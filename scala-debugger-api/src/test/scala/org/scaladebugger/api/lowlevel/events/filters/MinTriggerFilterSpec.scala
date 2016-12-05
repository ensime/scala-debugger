package org.scaladebugger.api.lowlevel.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class MinTriggerFilterSpec extends test.ParallelMockFunSpec
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
