package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.senkbeil.debugger.jdi.events.filters.JDIEventFilter

/**
 * Represents a processor for arguments for JDI Events. Evaluates the filters
 * in order, short-circuiting if a filter denies the event.
 *
 * @param arguments The collection of arguments to use
 */
class JDIEventArgumentProcessor(private val arguments: JDIEventArgument*) {
  /** Contains a collection of processors based on provided arguments. */
  private val processors = arguments.map(_.toProcessor)

  /**
   * Processes the event, applying any provided event arguments.
   *
   * @param event The event to process
   * @param forceAllArguments If true, forces all arguments to be evaluated,
   *                        regardless of whether an earlier argument denies
   *                        the event
   *
   * @return True if the event passes all of the arguments, otherwise false
   */
  def process(event: Event, forceAllArguments: Boolean = false): Boolean = {
    // Evaluate all filters and determine if all succeed
    if (forceAllArguments) processors.map(_.process(event)).forall(_ == true)

    // Evaluate filters until one is found that fails
    else processors.find(_.process(event) == false).isEmpty
  }
}
