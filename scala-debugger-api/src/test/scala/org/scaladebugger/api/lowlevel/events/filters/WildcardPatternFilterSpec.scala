package org.scaladebugger.api.lowlevel.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class WildcardPatternFilterSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
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
