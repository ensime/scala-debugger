package org.senkbeil.debugger.api.jdi.events.filters.processors

import com.sun.jdi.event.Event
import org.senkbeil.debugger.api.jdi.events.JDIEventArgument
import org.senkbeil.debugger.api.jdi.events.filters.{JDIEventFilterProcessor, CustomPropertyFilter}

/**
 * Represents a processor for the custom property filter.
 *
 * @param customPropertyFilter The custom property filter to use when processing
 */
class CustomPropertyFilterProcessor(
  val customPropertyFilter: CustomPropertyFilter
) extends JDIEventFilterProcessor {
  private val key = customPropertyFilter.key
  private val value = customPropertyFilter.value

  /**
   * Processes the provided event with the filter logic. Increments an internal
   * counter to compare against the desired count.
   *
   * @param event Unused
   *
   * @return True if the event passes through the filter, otherwise false; no
   *         data is included
   */
  override def process(event: Event): Boolean = {
    event.request().getProperty(key) == value
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = {}

  override val argument: JDIEventArgument = customPropertyFilter
}
