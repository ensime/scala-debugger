package org.senkbeil.debugger.api.jdi.events.filters.processors

import com.sun.jdi.event.{Event, LocatableEvent, MethodEntryEvent, MethodExitEvent}
import org.senkbeil.debugger.api.jdi.events.JDIEventArgument
import org.senkbeil.debugger.api.jdi.events.filters.{JDIEventFilterProcessor, MethodNameFilter}

/**
 * Represents a processor for the custom property filter.
 *
 * @param methodNameFilter The custom property filter to use when processing
 */
class MethodNameFilterProcessor(
  val methodNameFilter: MethodNameFilter
) extends JDIEventFilterProcessor {
  private val name = methodNameFilter.name

  /**
   * Processes the provided event with the filter logic.
   *
   * @param event The event to process
   *
   * @return True if the event passes through the filter, otherwise false
   */
  override def process(event: Event): Boolean = event match {
    case methodEntryEvent: MethodEntryEvent =>
      methodEntryEvent.method().name() == name
    case methodExitEvent: MethodExitEvent =>
      methodExitEvent.method().name() == name
    case locatableEvent: LocatableEvent =>
      locatableEvent.location().method().name() == name
    case _ =>
      true
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = {}

  override val argument: JDIEventArgument = methodNameFilter
}
