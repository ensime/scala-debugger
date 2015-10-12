package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.events.data.{JDIEventDataProcessor, JDIEventDataRequest}
import org.senkbeil.debugger.jdi.events.filters.{JDIEventFilterProcessor, JDIEventFilter}

import scala.reflect.ClassTag

class JDIEventArgumentProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  describe("JDIEventArgumentProcessor") {
    describe("#processFilters") {
      it("should process all filters if the force flag is set to true") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false, numberOfRuns = 1
        )

        // Should be invoked once, even though the filter before it denied
        // the event
        val acceptingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = true, numberOfRuns = 1
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        val result = jdiEventArgumentProcessor.processFilters(
          mockEvent,
          forceAllFilters = true
        )
      }

      it("should process the next filter if the previous filter accepted the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, accepting the event
        val acceptingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = true, numberOfRuns = 1
        )

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false, numberOfRuns = 1
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          acceptingArgumentAndProcessor._1,
          denyingArgumentAndProcessor._1
        )

        val result = jdiEventArgumentProcessor.processFilters(mockEvent)
      }

      it("should not process the next filter if the previous filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        val result = jdiEventArgumentProcessor.processFilters(mockEvent)
      }

      it("should return false if any filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.processFilters(mockEvent) should be (false)
      }

      it("should return true if all filters accepted the event") {
        val mockEvent = mock[Event]

        val acceptingArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = true, numberOfRuns = 2
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          acceptingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.processFilters(mockEvent) should be (true)
      }
    }
  }

  /**
   * Sets the expectations for the processor and sets up the event argument
   * to return the provided processor.
   *
   * @param mockArgument The mock argument to return the mock processor
   * @param mockProcessor The processor whose expectations to set
   * @param processReturnValue The return value for the process method of the
   *                           processor
   * @param numberOfRuns The number of times that the process method of the
   *                     processor will be invoked
   *
   * @return The tuple containing the filter and processor mocks
   */
  private def setExpectations(
    mockArgument: JDIEventArgument,
    mockProcessor: JDIEventProcessor,
    processReturnValue: Any,
    numberOfRuns: Int
  ): (JDIEventArgument, JDIEventProcessor) = {
    // Allow any number of times for arbitrary usage of function
    (mockArgument.toProcessor _).expects()
      .returning(mockProcessor).anyNumberOfTimes()

    (mockProcessor.process _).expects(*).returning(processReturnValue)
      .repeat(numberOfRuns).times()

    (mockArgument, mockProcessor)
  }
}
