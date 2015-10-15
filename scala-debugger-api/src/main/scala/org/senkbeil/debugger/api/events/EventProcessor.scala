package org.senkbeil.debugger.api.events

import com.sun.jdi.event.Event
import scala.util.{Failure, Success, Try}

/**
 * Represents a processor for a single event, evaluating any associated
 * functions and returning the result.
 *
 * @param event The event to process
 * @param eventFunctions The functions to evaluate using the provided event
 * @param onExceptionResume If true, exceptions thrown by event functions will
 *                          be ignored (not causing the overall result to fail)
 */
class EventProcessor(
  private val event: Event,
  private val eventFunctions: Seq[EventManager#EventHandler],
  private val onExceptionResume: Boolean
) {
  /**
   * Performs the actual processing of the event functions for the given event,
   * determining if the containing event set should be resumed.
   *
   * @return True if all event functions pass and the event set should be
   *         resumed, otherwise false
   */
  def process(): Boolean = {
    // If contains events, process each and get collective result
    if (eventFunctions != null && eventFunctions.nonEmpty) {
      // NOTE: The function being invoked SHOULD be the wrapped function that
      //       runs the filter and retrieves data that replaces the Nil below
      eventFunctions
        .map(func => Try(func(event, Nil)))
        .map(resumeOnResult)
        .reduce(_ && _)
    } else true // No event to process, so just allow the flag to pass on
  }

  /**
   * Indicates whether to resume based on the result of a function.
   *
   * @param result The function result in the form of a try
   *
   * @return If the result was a success, the function result is returned,
   *         otherwise the flag onExceptionResume is returned
   */
  @inline
  private def resumeOnResult(result: Try[Boolean]): Boolean = result match {
    case Success(r) => r
    case Failure(_) => onExceptionResume
  }
}
