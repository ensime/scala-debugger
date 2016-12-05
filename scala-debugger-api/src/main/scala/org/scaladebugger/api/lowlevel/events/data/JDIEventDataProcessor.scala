package org.scaladebugger.api.lowlevel.events.data

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.JDIEventProcessor

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

  /**
   * Represents the data request contained by this processor.
   *
   * @return The specific filter instance
   */
  override val argument: JDIEventDataRequest
}
