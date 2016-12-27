package org.scaladebugger.api.lowlevel.requests.properties

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class CustomPropertySpec extends ParallelMockFunSpec
{
  private val mockKey = mock[AnyRef]
  private val mockValue = mock[AnyRef]
  private val customProperty = CustomProperty(key = mockKey, value = mockValue)

  describe("CustomProperty") {
    describe("#toProcessor") {
      it("should return a processor containing the custom property") {
        customProperty.toProcessor.argument should be (customProperty)
      }
    }
  }
}
