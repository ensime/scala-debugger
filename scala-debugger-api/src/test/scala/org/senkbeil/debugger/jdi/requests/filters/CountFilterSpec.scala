package org.senkbeil.debugger.jdi.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class CountFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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
