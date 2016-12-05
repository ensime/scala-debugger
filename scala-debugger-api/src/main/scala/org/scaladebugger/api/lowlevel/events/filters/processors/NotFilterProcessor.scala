package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.filters.{JDIEventFilter, NotFilter, JDIEventFilterProcessor, OrFilter}

/**
 * Represents a processor for the 'not' filter.
 *
 * @param notFilter The 'not' filter to use when processing
 */
class NotFilterProcessor(
  val notFilter: NotFilter
) extends JDIEventFilterProcessor {
  private val filter = notFilter.filter

  /**
   * Processes the provided event with the filter logic.
   *
   * @param event Provided to the internal filter
   *
   * @return True if the event passes through the filter, otherwise false
   */
  override def process(event: Event): Boolean = {
    !filter.toProcessor.process(event)
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = {}

  override val argument: JDIEventFilter = notFilter
}
