package org.senkbeil.debugger.jdi.requests.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, FunSpec, Matchers}

class SourceNameFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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
