package org.scaladebugger.api.lowlevel.events.filters

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.JDIEventProcessor

/**
  * Represents a processor for a JDI Event Filter.
  */
trait JDIEventFilterProcessor extends JDIEventProcessor {
  /**
   * Processes the provided event.
   *
   * @param event The event to process
   *
   * @return True if the process was successful (the event passed the filter),
   *         otherwise false
   */
  override def process(event: Event): Boolean

  /**
   * Resets the internal state of the processor.
   */
  def reset(): Unit

  /**
   * Represents the filter contained by this processor.
   *
   * @return The specific filter instance
   */
  override val argument: JDIEventFilter
}
