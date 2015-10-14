package org.senkbeil.debugger.events

import com.sun.jdi.event.{EventIterator, EventSet, Event}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.events.EventType.EventType
import org.senkbeil.debugger.jdi.events.data.JDIEventDataResult

class EventSetProcessorSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest
{
  // TODO: This is a workaround for a log statement that is causing a test to
  //       fail if we don't mock the toString function
  private val mockEventType = EventType.AccessWatchpointEventType // mock[EventType]

  private val mockEvent = mock[Event]
  private val mockEventIterator = mock[EventIterator]
  private val mockEventSet = mock[EventSet]
  (mockEventSet.iterator _).expects().returning(mockEventIterator).once()
  private val mockEventFunctionRetrieval =
    mockFunction[EventType, Seq[(Event, Seq[JDIEventDataResult]) => Boolean]]
  private val mockEventFunctions = Seq(
    mockFunction[Event, Seq[JDIEventDataResult], Boolean],
    mockFunction[Event, Seq[JDIEventDataResult], Boolean]
  )

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class TestEventProcessor
    extends EventProcessor(mockEvent, mockEventFunctions, true)
  private val mockEventProcessor = mock[TestEventProcessor]

  private val mockNewEventProcessor =
    mockFunction[Event, Seq[EventManager#EventHandler], EventProcessor]
  private val mockTransformEventToEventType =
    mockFunction[Event, Option[EventType]]

  // Takes a single boolean used to set the onExceptionResume flag
  // NOTE: Not using partial function so we can use named parameters
  private def newEventSetProcessor(onExceptionResume: Boolean) =
    new EventSetProcessor(
      eventSet                = mockEventSet,
      eventFunctionRetrieval  = mockEventFunctionRetrieval,
      onExceptionResume = onExceptionResume
    ) {
      override protected def newEventProcessor(
        event: Event,
        eventFunctions: Seq[EventManager#EventHandler]
      ): EventProcessor = mockNewEventProcessor(event, eventFunctions)

      override protected def transformEventToEventType(
        event: Event
      ): Option[EventType] = mockTransformEventToEventType(event)
    }

  describe("EventSetProcessor") {
    describe("#process") {
      it("should return true and resume the event set if there are no events in the set") {
        val expected = true
        val eventSetProcessor = newEventSetProcessor(onExceptionResume = true)

        // If the event set had no events, its iterator would say so immediately
        (mockEventIterator.hasNext _).expects().returning(false).once()

        // The event set should be resumed
        (mockEventSet.resume _).expects().once()

        val actual = eventSetProcessor.process()

        actual should be (expected)
      }

      it("should return true and resume the event set if all of the events are unknown event types") {
        val expected = true
        val eventSetProcessor = newEventSetProcessor(onExceptionResume = true)

        inSequence {
          // Only retrieve a single event (which will be unknown)
          (mockEventIterator.hasNext _).expects().returning(true).once()
          (mockEventIterator.next _).expects().returning(mockEvent).once()
          mockTransformEventToEventType.expects(*).returning(None).once()

          // Should be false as we are out of events
          (mockEventIterator.hasNext _).expects().returning(false).once()

          // The event set should be resumed
          (mockEventSet.resume _).expects().once()
        }

        val actual = eventSetProcessor.process()

        actual should be (expected)
      }

      it("should return true and resume the event set if all event processors for events return true") {
        val expected = true
        val eventSetProcessor = newEventSetProcessor(onExceptionResume = true)

        inSequence {
          inAnyOrder {
            // Retrieve two events (which will be evaluated as true)
            (mockEventIterator.hasNext _).expects().returning(true).twice()
            (mockEventIterator.next _).expects().returning(mockEvent).twice()
            mockTransformEventToEventType.expects(*)
              .returning(Some(mockEventType)).twice()
            mockEventFunctionRetrieval.expects(*)
              .returning(mockEventFunctions).twice()
            mockNewEventProcessor.expects(*, *)
              .returning(mockEventProcessor).twice()

            // Evaluate both events as true
            (mockEventProcessor.process _).expects().returning(true).twice()
          }

          // Should be false as we are out of events
          (mockEventIterator.hasNext _).expects().returning(false).once()

          // The event set should be resumed
          (mockEventSet.resume _).expects().once()
        }

        val actual = eventSetProcessor.process()

        actual should be (expected)
      }

      it("should return false and not resume the event set if any event processor for an event returns false") {
        val expected = false
        val eventSetProcessor = newEventSetProcessor(onExceptionResume = true)

        inSequence {
          inAnyOrder {
            // Retrieve two events (which will be evaluated as true)
            (mockEventIterator.hasNext _).expects().returning(true).twice()
            (mockEventIterator.next _).expects().returning(mockEvent).twice()
            mockTransformEventToEventType.expects(*)
              .returning(Some(mockEventType)).twice()
            mockEventFunctionRetrieval.expects(*)
              .returning(mockEventFunctions).twice()
            mockNewEventProcessor.expects(*, *)
              .returning(mockEventProcessor).twice()

            // Evaluate one as true and the other as false
            (mockEventProcessor.process _).expects().returning(false).once()
            (mockEventProcessor.process _).expects().returning(true).once()
          }

          // Should be false as we are out of events
          (mockEventIterator.hasNext _).expects().returning(false).once()

          // The event set should not be resumed
          (mockEventSet.resume _).expects().never()
        }

        val actual = eventSetProcessor.process()

        actual should be (expected)
      }
    }

    describe("#resume") {
      it("should resume the wrapped event set") {
        val eventSetProcessor = newEventSetProcessor(onExceptionResume = true)

        // The resume method merely wraps the event set's resume
        (mockEventSet.resume _).expects().once()

        eventSetProcessor.resume()
      }
    }
  }
}
