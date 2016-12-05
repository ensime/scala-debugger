package org.scaladebugger.api.lowlevel.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ClassInclusionFilterSpec extends test.ParallelMockFunSpec
{
  private val testPattern = "some pattern"
  private val classInclusionFilter = ClassInclusionFilter(
    classPattern = testPattern
  )

  describe("ClassInclusionFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the class inclusion filter") {
        classInclusionFilter.toProcessor.argument should be (classInclusionFilter)
      }
    }
  }
}
