package org.scaladebugger.api.lowlevel.requests.properties

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.properties.processors.CustomPropertyProcessor

class UniqueIdPropertySpec extends test.ParallelMockFunSpec
{
  private val testId = java.util.UUID.randomUUID().toString
  private val uniqueIdProperty = UniqueIdProperty(id = testId)

  describe("UniqueIdProperty") {
    describe("constructor") {
      it("should set the custom property key to \"_id\"") {
        uniqueIdProperty.key should be ("_id")
      }

      it("should set the custom property value to the id") {
        uniqueIdProperty.value should be (testId)
      }
    }
    describe("#toProcessor") {
      it("should return a custom property processor") {
        uniqueIdProperty.toProcessor shouldBe a [CustomPropertyProcessor]
      }

      it("should return a processor containing the unique id property") {
        uniqueIdProperty.toProcessor.argument should be (uniqueIdProperty)
      }
    }
  }
}
