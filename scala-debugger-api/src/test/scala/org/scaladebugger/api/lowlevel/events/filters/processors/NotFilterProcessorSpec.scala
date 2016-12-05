package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.{NotFilter, JDIEventFilter, JDIEventFilterProcessor}

class NotFilterProcessorSpec extends test.ParallelMockFunSpec
{
  describe("NotFilterProcessor") {
    describe("#process") {
      it ("should return false if any internal filter returns false") {
        val expected = false

        val notProcessor = new NotFilterProcessor(NotFilter(
          newMockFilter(result = true)
        ))

        val actual = notProcessor.process(mock[Event])

        actual should be (expected)
      }

      it("should return true if the internal filter returns false") {
        val expected = true

        val notProcessor = new NotFilterProcessor(NotFilter(
          newMockFilter(result = false)
        ))

        val actual = notProcessor.process(mock[Event])

        actual should be (expected)
      }
    }
  }

  private def newMockFilter(result: Boolean): JDIEventFilter = {
    val mockFilter = mock[JDIEventFilter]
    val mockProcessor = mock[JDIEventFilterProcessor]

    (mockFilter.toProcessor _).expects().returning(mockProcessor).once()
    (mockProcessor.process _).expects(*).returning(result).once()

    mockFilter
  }
}
