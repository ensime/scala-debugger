package org.senkbeil.debugger.api.jdi.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class ClassExclusionFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val testPattern = "some pattern"
  private val classExclusionFilter = ClassExclusionFilter(
    classPattern = testPattern
  )

  describe("ClassExclusionFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the class exclusion filter") {
        classExclusionFilter.toProcessor.argument should be (classExclusionFilter)
      }
    }
  }
}
