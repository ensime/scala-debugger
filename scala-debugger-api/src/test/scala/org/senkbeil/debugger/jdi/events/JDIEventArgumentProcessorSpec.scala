package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.jdi.events.data.{JDIEventDataResult, JDIEventDataProcessor, JDIEventDataRequest}
import org.senkbeil.debugger.jdi.events.filters.{JDIEventFilterProcessor, JDIEventFilter}

class JDIEventArgumentProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  describe("JDIEventArgumentProcessor") {
    describe("#processAll") {
      it("should process all filters and, if true, data and other arguments") {
        val expected = (true, Seq(mock[JDIEventDataResult]), Seq(new Object))
        val mockEvent = mock[Event]

        // Yields true for filters, so data/other should be processed
        val filterArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = expected._1,
          numberOfRuns = 1
        )

        val dataArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventDataRequest],
          mockProcessor = mock[JDIEventDataProcessor],
          processReturnValue = expected._2,
          numberOfRuns = 1
        )

        val otherArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventArgument],
          mockProcessor = mock[JDIEventProcessor],
          processReturnValue = expected._3.head,
          numberOfRuns = 1
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          dataArgumentAndProcessor._1,
          otherArgumentAndProcessor._1,
          filterArgumentAndProcessor._1
        )

        val actual = jdiEventArgumentProcessor.processAll(mockEvent)

        actual._1 should be (expected._1)
        actual._2 should contain theSameElementsInOrderAs (expected._2)
        actual._3 should contain theSameElementsInOrderAs (expected._3)
      }

      it("should process all filters and nothing else if filters yields false") {
        val expected = (false, Nil, Nil)
        val mockEvent = mock[Event]

        // Yields true for filters, so data/other should be processed
        val filterArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = expected._1,
          numberOfRuns = 1
        )

        // Should not be evaluated
        val dataArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventDataRequest],
          mockProcessor = mock[JDIEventDataProcessor],
          processReturnValue = expected._2,
          numberOfRuns = 0
        )

        // Should not be evaluated
        val otherArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventArgument],
          mockProcessor = mock[JDIEventProcessor],
          processReturnValue = expected._3,
          numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          dataArgumentAndProcessor._1,
          otherArgumentAndProcessor._1,
          filterArgumentAndProcessor._1
        )

        val actual = jdiEventArgumentProcessor.processAll(mockEvent)

        actual._1 should be (expected._1)
        actual._2 should contain theSameElementsInOrderAs (expected._2)
        actual._3 should contain theSameElementsInOrderAs (expected._3)
      }
    }

    describe("#processData") {
      it("should process all data requests in order") {
        val expected = Seq(mock[JDIEventDataResult], mock[JDIEventDataResult])
        val mockEvent = mock[Event]

        // Should not be evaluated
        val otherArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventArgument],
          mockProcessor = mock[JDIEventProcessor],
          processReturnValue = null,
          numberOfRuns = 0
        )

        val data1ArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventDataRequest],
          mockProcessor = mock[JDIEventDataProcessor],
          processReturnValue = Seq(expected.head),
          numberOfRuns = 1
        )

        val data2ArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventDataRequest],
          mockProcessor = mock[JDIEventDataProcessor],
          processReturnValue = Seq(expected.last),
          numberOfRuns = 1
        )

        // Should not be evaluated
        val filterArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false,
          numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          data1ArgumentAndProcessor._1,
          otherArgumentAndProcessor._1,
          data2ArgumentAndProcessor._1,
          filterArgumentAndProcessor._1
        )

        val actual = jdiEventArgumentProcessor.processData(mockEvent)

        actual should contain theSameElementsInOrderAs (expected)
      }
    }

    describe("#processOther") {
      it("should process all other arguments in order") {
        val expected = Seq(new Object, new Object)
        val mockEvent = mock[Event]

        val other1ArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventArgument],
          mockProcessor = mock[JDIEventProcessor],
          processReturnValue = expected.head,
          numberOfRuns = 1
        )

        val other2ArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventArgument],
          mockProcessor = mock[JDIEventProcessor],
          processReturnValue = expected.last,
          numberOfRuns = 1
        )

        // Should not be evaluated
        val dataArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventDataRequest],
          mockProcessor = mock[JDIEventDataProcessor],
          processReturnValue = mock[JDIEventDataResult],
          numberOfRuns = 0
        )

        // Should not be evaluated
        val filterArgumentAndProcessor = setExpectations(
          mockArgument = mock[JDIEventFilter],
          mockProcessor = mock[JDIEventFilterProcessor],
          processReturnValue = false,
          numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          dataArgumentAndProcessor._1,
          other1ArgumentAndProcessor._1,
          filterArgumentAndProcessor._1,
          other2ArgumentAndProcessor._1
        )

        val actual = jdiEventArgumentProcessor.processOther(mockEvent)

        actual should contain theSameElementsInOrderAs (expected)
      }
    }

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
