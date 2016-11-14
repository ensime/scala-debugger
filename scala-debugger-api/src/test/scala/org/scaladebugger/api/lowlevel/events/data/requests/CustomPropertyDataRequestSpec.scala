package org.scaladebugger.api.lowlevel.events.data.requests
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class CustomPropertyDataRequestSpec extends test.ParallelMockFunSpec
{
  private val testKey = "some key"
  private val customPropertyDataRequest =
    CustomPropertyDataRequest(key = testKey)

  describe("CustomPropertyDataRequest") {
    describe("#toProcessor") {
      it("should return a processor containing the custom property data request") {
        customPropertyDataRequest.toProcessor.argument should
          be (customPropertyDataRequest)
      }
    }
  }
}
