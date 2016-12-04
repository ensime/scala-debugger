package org.scaladebugger.api.lowlevel.events.filters.processors
//import acyclic.file

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.filters.{CustomPropertyFilterLike, JDIEventFilter, JDIEventFilterProcessor, CustomPropertyFilter}

/**
 * Represents a processor for the custom property filter.
 *
 * @param customPropertyFilter The custom property filter to use when processing
 */
class CustomPropertyFilterProcessor(
  val customPropertyFilter: JDIEventFilter with CustomPropertyFilterLike
) extends JDIEventFilterProcessor {
  private val key = customPropertyFilter.key
  private val value = customPropertyFilter.value

  /**
   * Processes the provided event with the filter logic.
   *
   * @param event The event to process
   *
   * @return True if the event passes through the filter, otherwise false
   */
  override def process(event: Event): Boolean = {
    Option(event.request())
      .flatMap(r => Option(r.getProperty(key)))
      .exists(_ == value)
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = {}

  override val argument: JDIEventFilter = customPropertyFilter
}
