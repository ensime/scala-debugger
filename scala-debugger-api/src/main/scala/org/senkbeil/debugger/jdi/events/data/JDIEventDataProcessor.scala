package org.senkbeil.debugger.jdi.events.data

import com.sun.jdi.event.Event
import org.senkbeil.debugger.jdi.events.JDIEventProcessor

/**
 * Represents a processor for data retrieval from an event.
 */
trait JDIEventDataProcessor extends JDIEventProcessor {
  /**
   * Processes the provided event to retrieve the requested data.
   *
   * @param event The event to process
   *
   * @return The collection of results from processing the event
   */
  override def process(event: Event): Seq[JDIEventDataResult]
}
