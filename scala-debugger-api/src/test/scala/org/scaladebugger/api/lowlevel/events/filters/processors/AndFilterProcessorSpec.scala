package org.scaladebugger.api.lowlevel.events.filters.processors
import acyclic.file

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.{JDIEventFilter, JDIEventFilterProcessor, AndFilter}

class AndFilterProcessorSpec extends test.ParallelMockFunSpec
{
  describe("AndFilterProcessor") {
    describe("#process") {
      it ("should return false if any internal filter returns false") {
        val expected = false

        val andProcessor = new AndFilterProcessor(AndFilter(
          newMockFilter(result = true),
          newMockFilter(result = false)
        ))

        val actual = andProcessor.process(mock[Event])

        actual should be (expected)
      }

      it("should return true if all internal filters return true") {
        val expected = true

        val andProcessor = new AndFilterProcessor(AndFilter(
          newMockFilter(result = true),
          newMockFilter(result = true)
        ))

        val actual = andProcessor.process(mock[Event])

        actual should be (expected)
      }

      it("should return true if there are no internal filters") {
        val expected = true

        val andProcessor = new AndFilterProcessor(AndFilter())

        val actual = andProcessor.process(mock[Event])

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
