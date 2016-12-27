package org.scaladebugger.api.lowlevel.requests.filters

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class CountFilterSpec extends ParallelMockFunSpec
{
  private val testCount = 3
  private val countFilter = CountFilter(count = testCount)

  describe("CountFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the count filter") {
        countFilter.toProcessor.argument should be (countFilter)
      }
    }
  }
}
