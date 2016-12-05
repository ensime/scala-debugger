package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.{OrFilter, JDIEventFilter, JDIEventFilterProcessor}

class OrFilterProcessorSpec extends test.ParallelMockFunSpec
{
  describe("OrFilterProcessor") {
    describe("#process") {
      it ("should return false if all internal filters return false") {
        val expected = false

        val orProcessor = new OrFilterProcessor(OrFilter(
          newMockFilter(result = false),
          newMockFilter(result = false)
        ))

        val actual = orProcessor.process(mock[Event])

        actual should be (expected)
      }

      it("should return true if any internal filter returns true") {
        val expected = true

        val orProcessor = new OrFilterProcessor(OrFilter(
          newMockFilter(result = true),
          newMockFilter(result = false)
        ))

        val actual = orProcessor.process(mock[Event])

        actual should be (expected)
      }

      it("should return true if there are no internal filters") {
        val expected = true

        val orProcessor = new OrFilterProcessor(OrFilter())

        val actual = orProcessor.process(mock[Event])

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
