package org.scaladebugger.api.lowlevel.requests.filters

import com.sun.jdi.request.EventRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestProcessor

/**
  * Represents a processor for a JDI Event Filter.
  */
trait JDIRequestFilterProcessor extends JDIRequestProcessor {
  /**
   * Processes the provided event request using the filter.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest

  /**
   * Represents the filter contained by this processor.
   *
   * @return The specific filter instance
   */
  override val argument: JDIRequestFilter
}
