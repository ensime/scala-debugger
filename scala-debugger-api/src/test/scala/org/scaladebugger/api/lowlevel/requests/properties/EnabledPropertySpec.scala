package org.scaladebugger.api.lowlevel.requests.properties
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class EnabledPropertySpec extends test.ParallelMockFunSpec
{
  private val testValue = false
  private val enabledProperty = EnabledProperty(value = testValue)

  describe("EnabledProperty") {
    describe("#toProcessor") {
      it("should return a processor containing the enabled property") {
        enabledProperty.toProcessor.argument should be (enabledProperty)
      }
    }
  }
}
