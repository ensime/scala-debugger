package org.scaladebugger.api.lowlevel.requests.properties.processors

import com.sun.jdi.request._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.properties.CustomProperty

class CustomPropertyProcessorSpec extends test.ParallelMockFunSpec
{
  private val mockKey = mock[AnyRef]
  private val mockValue = mock[AnyRef]
  private val customProperty = CustomProperty(
    key = mockKey,
    value = mockValue
  )
  private val customPropertyProcessor =
    new CustomPropertyProcessor(customProperty)

  describe("CustomPropertyProcessor") {
    describe("#process") {
      it("should add the property to the event request") {
        val mockEventRequest = mock[EventRequest]

        (mockEventRequest.putProperty _).expects(mockKey, mockValue).once()

        customPropertyProcessor.process(mockEventRequest)
      }
    }
  }
}
