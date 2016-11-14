package org.scaladebugger.api.lowlevel.events.filters.processors
import acyclic.file

import com.sun.jdi.event.Event
import com.sun.jdi.request.EventRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.CustomPropertyFilter

class CustomPropertyFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testKey = "some key"
  private val testValue = "some value"
  private val customPropertyFilter = CustomPropertyFilter(
    key = testKey,
    value = testValue
  )
  private val customPropertyProcessor =
    new CustomPropertyFilterProcessor(customPropertyFilter)

  describe("CustomPropertyFilterProcessor") {
    describe("#process") {
      it("should return false if the property is not found") {
        val expected = false

        val mockEvent = mock[Event]
        val mockRequest = mock[EventRequest]

        // Request's property is null if not found
        inSequence {
          (mockEvent.request _).expects().returning(mockRequest).once()
          (mockRequest.getProperty _).expects(testKey).returning(null).once()
        }

        val actual = customPropertyProcessor.process(mockEvent)
        actual should be (expected)
      }

      it ("should return false if the property does not match") {
        val expected = false

        val mockEvent = mock[Event]
        val mockRequest = mock[EventRequest]

        // Request's property is different
        inSequence {
          (mockEvent.request _).expects().returning(mockRequest).once()
          (mockRequest.getProperty _).expects(testKey)
            .returning(testValue+1).once()
        }

        val actual = customPropertyProcessor.process(mockEvent)
        actual should be (expected)
      }

      it("should return true if the property matches") {
        val expected = true

        val mockEvent = mock[Event]
        val mockRequest = mock[EventRequest]

        // Request's property is same
        inSequence {
          (mockEvent.request _).expects().returning(mockRequest).once()
          (mockRequest.getProperty _).expects(testKey)
            .returning(testValue).once()
        }

        val actual = customPropertyProcessor.process(mockEvent)
        actual should be (expected)
      }
    }
  }
}
