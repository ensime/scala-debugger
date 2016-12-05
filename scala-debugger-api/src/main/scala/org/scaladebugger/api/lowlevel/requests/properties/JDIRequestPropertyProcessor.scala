package org.scaladebugger.api.lowlevel.requests.properties

import com.sun.jdi.request.EventRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestProcessor

/**
  * Represents a processor for a JDI Event Filter.
  */
trait JDIRequestPropertyProcessor extends JDIRequestProcessor {
  /**
   * Processes the provided event request using the property.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest

  /**
   * Represents the property contained by this processor.
   *
   * @return The specific property instance
   */
  override val argument: JDIRequestProperty
}
