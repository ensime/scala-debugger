package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult

class EventProcessorSpec extends FunSpec with Matchers with MockFactory
  with ParallelTestExecution
{
  private val mockEvent = mock[Event]
  private val mockEventFunctions = Seq(
    mockFunction[Event, Seq[JDIEventDataResult], Boolean],
    mockFunction[Event, Seq[JDIEventDataResult], Boolean]
  )

  // Takes a single boolean used to set the onExceptionResume flag
  // NOTE: Not using partial function so we can use named parameters
  private def newEventProcessor(onExceptionResume: Boolean) = new EventProcessor(
    event             = mockEvent,
    eventFunctions    = mockEventFunctions,
    onExceptionResume = onExceptionResume
  )

  describe("EventProcessor") {
    describe("#process") {
      describe("onExceptionResume == true") {
        it("should return true if all functions are successful and return true") {
          val expected = true
          val eventProcessor = newEventProcessor(onExceptionResume = true)

          // All functions are successful and return true
          mockEventFunctions.foreach(
            _.expects(mockEvent, Nil).returning(true).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }

        it("should return true if one function fails and all others return true") {
          val expected = true
          val eventProcessor = newEventProcessor(onExceptionResume = true)

          // First function fails
          mockEventFunctions.head.expects(mockEvent, Nil)
            .throwing(new Throwable).once()

          // All other functions are successful and return true
          mockEventFunctions.tail.foreach(
            _.expects(mockEvent, Nil).returning(true).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }

        it("should return false if all functions are successful but one returns false") {
          val expected = false
          val eventProcessor = newEventProcessor(onExceptionResume = true)

          // First function is successful and returns false
          mockEventFunctions.head.expects(mockEvent, Nil).returning(true).once()

          // All functions are successful and return false
          mockEventFunctions.tail.foreach(
            _.expects(mockEvent, Nil).returning(false).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }
      }

      describe("onExceptionResume == false") {
        it("should return true if all functions are successful and return true") {
          val expected = true
          val eventProcessor = newEventProcessor(onExceptionResume = false)

          // All functions are successful and return true
          mockEventFunctions.foreach(
            _.expects(mockEvent, Nil).returning(true).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }

        it("should return false if one function fails and all others return true") {
          val expected = false
          val eventProcessor = newEventProcessor(onExceptionResume = false)

          // First function fails
          mockEventFunctions.head.expects(mockEvent, Nil)
            .throwing(new Throwable).once()

          // All other functions are successful and return true
          mockEventFunctions.tail.foreach(
            _.expects(mockEvent, Nil).returning(true).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }

        it("should return false if all functions are successful but one returns false") {
          val expected = false
          val eventProcessor = newEventProcessor(onExceptionResume = false)

          // First function is successful and returns false
          mockEventFunctions.head.expects(mockEvent, Nil).returning(true).once()

          // All other functions are successful and return true
          mockEventFunctions.tail.foreach(
            _.expects(mockEvent, Nil).returning(false).once()
          )

          val actual = eventProcessor.process()

          actual should be (expected)
        }
      }
    }
  }
}
