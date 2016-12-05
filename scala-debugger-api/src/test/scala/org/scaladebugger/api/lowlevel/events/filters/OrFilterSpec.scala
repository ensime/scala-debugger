package org.scaladebugger.api.lowlevel.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class OrFilterSpec extends test.ParallelMockFunSpec
{
  private val orFilter = OrFilter()

  describe("OrFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the or filter") {
        orFilter.toProcessor.argument should be (orFilter)
      }
    }
  }
}
