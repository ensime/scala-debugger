package org.scaladebugger.api.lowlevel.events.data.processors

import com.sun.jdi.event.Event
import com.sun.jdi.request.EventRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.data.requests.CustomPropertyDataRequest
import org.scaladebugger.api.lowlevel.events.data.results.CustomPropertyDataResult
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class CustomPropertyDataRequestProcessorSpec extends ParallelMockFunSpec
{
  private val testKey = "some key"
  private val testValue = "some value"
  private val customPropertyDataRequest =
    CustomPropertyDataRequest(key = testKey)
  private val customPropertyDataProcessor =
    new CustomPropertyDataRequestProcessor(customPropertyDataRequest)

  describe("CustomPropertyDataRequestProcessor") {
    describe("#process") {
      it ("should return a result object if the property is found") {
        val expected = CustomPropertyDataResult(
          key = testKey,
          value = testValue
        )

        val mockRequest = mock[EventRequest]
        val mockEvent = mock[Event]

        // Retrieval of the property should return our test value, meaning that
        // there is a property
        (mockEvent.request _).expects().returning(mockRequest).once()
        (mockRequest.getProperty _).expects(testKey).returning(testValue).once()

        val actual = customPropertyDataProcessor.process(mockEvent)
        actual should contain only (expected)
      }

      it("should return an empty collection if the property is not found") {
        val expected = CustomPropertyDataResult(
          key = testKey,
          value = testValue
        )

        val mockRequest = mock[EventRequest]
        val mockEvent = mock[Event]

        // Retrieval of the property should return null, meaning there is no
        // valid property
        (mockEvent.request _).expects().returning(mockRequest).once()
        (mockRequest.getProperty _).expects(testKey).returning(null).once()

        val actual = customPropertyDataProcessor.process(mockEvent)
        actual should be (empty)
      }
    }
  }
}
