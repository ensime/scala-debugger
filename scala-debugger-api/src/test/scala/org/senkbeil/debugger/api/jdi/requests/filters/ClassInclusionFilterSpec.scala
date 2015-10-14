package org.senkbeil.debugger.api.jdi.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class ClassInclusionFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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
