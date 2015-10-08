package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class JDIEventArgumentProcessorSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  describe("JDIEventArgumentProcessor") {
    describe("#process") {
      it("should process all filters if the force flag is set to true") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should be invoked once, even though the filter before it denied
        // the event
        val acceptingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = true, numberOfRuns = 1
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.process(mockEvent, forceAllArguments = true)
      }

      it("should process the next filter if the previous filter accepted the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, accepting the event
        val acceptingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = true, numberOfRuns = 1
        )

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          acceptingArgumentAndProcessor._1,
          denyingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.process(mockEvent)
      }

      it("should not process the next filter if the previous filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.process(mockEvent)
      }

      it("should return false if any filter denied the event") {
        val mockEvent = mock[Event]

        // Should be invoked once, denying the event
        val denyingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = false, numberOfRuns = 1
        )

        // Should not be invoked since the first filter denied the event
        val acceptingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = true, numberOfRuns = 0
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          denyingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.process(mockEvent) should be (false)
      }

      it("should return true if all filters accepted the event") {
        val mockEvent = mock[Event]

        val acceptingArgumentAndProcessor = newMockArgumentAndProcessor(
          processReturnValue = true, numberOfRuns = 2
        )

        val jdiEventArgumentProcessor = new JDIEventArgumentProcessor(
          acceptingArgumentAndProcessor._1,
          acceptingArgumentAndProcessor._1
        )

        jdiEventArgumentProcessor.process(mockEvent) should be (true)
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
  private def newMockArgumentAndProcessor(
    processReturnValue: Boolean,
    numberOfRuns: Int
  ): (JDIEventArgument, JDIEventProcessor) = {
    val filterAndProcessor = newMockArgumentAndProcessor()

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
  private def newMockArgumentAndProcessor(): (JDIEventArgument, JDIEventProcessor) = {
    val mockArgument = mock[JDIEventArgument]
    val mockProcessor = mock[JDIEventProcessor]

    // Allow any number of times for arbitrary usage of function
    (mockArgument.toProcessor _).expects()
      .returning(mockProcessor).anyNumberOfTimes()

    (mockArgument, mockProcessor)
  }
}
