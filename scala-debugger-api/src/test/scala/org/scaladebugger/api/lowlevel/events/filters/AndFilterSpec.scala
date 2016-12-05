package org.scaladebugger.api.lowlevel.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class AndFilterSpec extends test.ParallelMockFunSpec
{
  private val andFilter = AndFilter()

  describe("AndFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the and filter") {
        andFilter.toProcessor.argument should be (andFilter)
      }
    }
  }
}
