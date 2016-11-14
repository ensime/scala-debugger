package org.scaladebugger.api.lowlevel.requests.filters
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class CountFilterSpec extends test.ParallelMockFunSpec
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
