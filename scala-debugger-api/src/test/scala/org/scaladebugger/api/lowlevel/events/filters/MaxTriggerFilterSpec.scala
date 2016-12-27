package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class MaxTriggerFilterSpec extends ParallelMockFunSpec
{
  private val testCount = 3
  private val maxTriggerFilter = MaxTriggerFilter(count = testCount)

  describe("MaxTriggerFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the max trigger filter") {
        maxTriggerFilter.toProcessor.argument should be (maxTriggerFilter)
      }
    }
  }
}
