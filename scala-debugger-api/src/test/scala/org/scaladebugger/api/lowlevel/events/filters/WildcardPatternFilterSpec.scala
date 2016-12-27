package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class WildcardPatternFilterSpec extends ParallelMockFunSpec
{
  private val testPattern = "some*pattern"
  private val wildcardPatternFilter = WildcardPatternFilter(
    pattern = testPattern
  )

  describe("WildcardPatternFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the wildcard pattern filter") {
        wildcardPatternFilter.toProcessor.argument should be (wildcardPatternFilter)
      }
    }
  }
}
