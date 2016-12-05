package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event.{Event, EventSet}
import EventType.EventType
import com.sun.jdi.VMDisconnectedException
import org.scaladebugger.api.utils.Logging

/**
 * Represents a processor for an event set, evaluating any associated
 * functions with each event in the set and returning the collective results.
 *
 * @param eventSet The set of events to process
 * @param eventFunctionRetrieval Used to retrieve event functions for the
 *                               specified event
 * @param onExceptionResume If true, exceptions thrown by event functions will
 *                          be ignored (not causing the overall result to fail)
 */
class EventSetProcessor(
  private val eventSet: EventSet,
  private val eventFunctionRetrieval: EventType => Seq[EventManager#EventHandler],
  private val onExceptionResume: Boolean
) extends Logging {
  /** Represents the iterator over the provided set of events. */
  private val eventSetIterator = eventSet.iterator()

  /**
   * Performs the actual task of extracting each event from the set of events,
   * evaluating it, and returning the collective results of all of the
   * evaluated events.
   *
   * @return True if all events pass and the event set was resumed,
   *         otherwise false
   */
  def process(): Boolean = {
    // Flag used to indicate whether or not to resume the event set
    @volatile var resumeFlag = true

    // NOTE: Event sets are grouped into common events, so there is no need
    //       to worry about the resume flag being affected by different types
    //       of events
    try {
      while (eventSetIterator.hasNext) {
        val event = eventSetIterator.next()
        val eventType = transformEventToEventType(event)

        // If an associated event type was found for the event, process it
        eventType.foreach(et => {
          logger.trace(s"Processing event: ${et.toString}")

          // Retrieve the functions for the event type and evaluate each one,
          // combining the results into an "all true or nothing"
          val eventFunctions = eventFunctionRetrieval(et)

          // NOTE: Moved out of inline &&= as that has short-circuit logic that
          //       results in not even processing the event, which is NOT what
          //       we want to happen (no "missing" events)
          val result = newEventProcessor(event, eventFunctions).process()
          resumeFlag &&= result
        })
      }
    } catch {
      case e: VMDisconnectedException =>
        logger.warn("Event set processing when VM disconnected", e)
      case t: Throwable =>
        logger.error("Error in event set processing", t)
    }

    // If result is that the event set should be resumed, then resume it
    if (resumeFlag) resume()

    resumeFlag
  }

  /**
   * Resumes the event set.
   */
  def resume(): Unit = eventSet.resume()

  /**
   * Transforms the given event to a corresponding event type.
   *
   * @param event The event to transform
   *
   * @return Some event type if the event is recognized, otherwise None
   */
  protected def transformEventToEventType(event: Event): Option[EventType] =
    EventType.eventToEventType(event)

  /**
   * Creates a new event processor. Can be overridden.
   *
   * @param event The event to process
   * @param eventFunctions The collection of functions to use when processing
   *                       the event
   *
   * @return The new event processor instance
   */
  protected def newEventProcessor(
    event: Event,
    eventFunctions: Seq[EventManager#EventHandler]
  ): EventProcessor = new EventProcessor(
    event             = event,
    eventFunctions    = eventFunctions,
    onExceptionResume = onExceptionResume
  )
}
