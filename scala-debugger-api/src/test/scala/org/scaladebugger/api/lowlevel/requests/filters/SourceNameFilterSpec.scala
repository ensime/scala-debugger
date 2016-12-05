package org.scaladebugger.api.lowlevel.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, FunSpec, Matchers}

class SourceNameFilterSpec extends test.ParallelMockFunSpec
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
