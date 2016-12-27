package org.scaladebugger.api.lowlevel.requests.filters

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class SourceNameFilterSpec extends ParallelMockFunSpec
{
  private val testPattern = "some pattern"
  private val sourceNameFilter = SourceNameFilter(
    sourceNamePattern = testPattern
  )

  describe("SourceNameFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the source name filter") {
        sourceNameFilter.toProcessor.argument should be (sourceNameFilter)
      }
    }
  }
}
