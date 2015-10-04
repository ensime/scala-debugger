package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event
import org.senkbeil.debugger.jdi.events.filters.JDIEventFilter

/**
 * Represents a processor for filters for JDI Events. Evaluates the filters
 * in order, short-circuiting if a filter denies the event.
 *
 * @param filters The collection of filters to use
 */
class JDIEventFilterProcessor(private val filters: JDIEventFilter*) {
  /** Contains a collection of processors based on provided filters. */
  private val processors = filters.map(_.toProcessor)

  /**
   * Processes the event, applying any provided event filters.
   *
   * @param event The event to process
   * @param forceAllFilters If true, forces all filters to be evaluated,
   *                        regardless of whether an earlier filter denies
   *                        the event
   *
   * @return True if the event passes all of the filters, otherwise false
   */
  def process(event: Event, forceAllFilters: Boolean = false): Boolean = {
    // Evaluate all filters and determine if all succeed
    if (forceAllFilters) processors.map(_.process(event)).forall(_ == true)

    // Evaluate filters until one is found that fails
    else processors.find(_.process(event) == false).isEmpty
  }
}
