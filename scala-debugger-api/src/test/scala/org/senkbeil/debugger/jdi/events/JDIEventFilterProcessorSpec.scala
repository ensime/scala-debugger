package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.events.filters.JDIEventFilter
import org.senkbeil.debugger.jdi.events.processors.JDIEventProcessor

class JDIEventFilterProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  describe("JDIEventFilterProcessor") {
    describe("#process") {
      it("should process all filters if the force flag is set to true") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should be invoked once, even though the filter before it denied
        // the event
        val acceptingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = true, numberOfRuns = 1
        )

        val jdiEventFilterProcessor = new JDIEventFilterProcessor(
          denyingFilterAndProcessor._1,
          acceptingFilterAndProcessor._1
        )

        jdiEventFilterProcessor.process(mockEvent, forceAllFilters = true)
      }

      it("should process the next filter if the previous filter accepted the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, accepting the event
        val acceptingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = true, numberOfRuns = 1
        )

        // Should be invoked once, denying the event
        val denyingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        val jdiEventFilterProcessor = new JDIEventFilterProcessor(
          acceptingFilterAndProcessor._1,
          denyingFilterAndProcessor._1
        )

        jdiEventFilterProcessor.process(mockEvent)
      }

      it("should not process the next filter if the previous filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventFilterProcessor = new JDIEventFilterProcessor(
          denyingFilterAndProcessor._1,
          acceptingFilterAndProcessor._1
        )

        jdiEventFilterProcessor.process(mockEvent)
      }

      it("should return false if any filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventFilterProcessor = new JDIEventFilterProcessor(
          denyingFilterAndProcessor._1,
          acceptingFilterAndProcessor._1
        )

        jdiEventFilterProcessor.process(mockEvent) should be (false)
      }

      it("should return true if all filters accepted the event") {
        val mockEvent = mock[Event]

        val acceptingFilterAndProcessor = newMockFilterAndProcessor(
          processReturnValue = true, numberOfRuns = 2
        )

        val jdiEventFilterProcessor = new JDIEventFilterProcessor(
          acceptingFilterAndProcessor._1,
          acceptingFilterAndProcessor._1
        )

        jdiEventFilterProcessor.process(mockEvent) should be (true)
      }
    }
  }

  /**
   * Creates a new filter (mock) that returns the same processor (mock)
   * every time.
   *
   * @param processReturnValue The return value for the process method of the
   *                           processor
   * @param numberOfRuns The number of times that the process method of the
   *                     processor will be invoked
   *
   * @return The tuple containing the filter and processor mocks
   */
  private def newMockFilterAndProcessor(
    processReturnValue: Boolean,
    numberOfRuns: Int
  ): (JDIEventFilter, JDIEventProcessor) = {
    val filterAndProcessor = newMockFilterAndProcessor()

    val mockProcessor = filterAndProcessor._2
    (mockProcessor.process _).expects(*).returning(processReturnValue)
      .repeat(numberOfRuns).times()

    filterAndProcessor
  }

  /**
   * Creates a new filter (mock) that returns the same processor (mock)
   * every time.
   *
   * @return The tuple containing the filter and processor mocks
   */
  private def newMockFilterAndProcessor(): (JDIEventFilter, JDIEventProcessor) = {
    val mockFilter = mock[JDIEventFilter]
    val mockProcessor = mock[JDIEventProcessor]

    // Allow any number of times for arbitrary usage of function
    (mockFilter.toProcessor _).expects()
      .returning(mockProcessor).anyNumberOfTimes()

    (mockFilter, mockProcessor)
  }
}
