package org.scaladebugger.api.lowlevel.events.data.requests

import org.scaladebugger.api.lowlevel.events.JDIEventProcessor
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataRequest
import org.scaladebugger.api.lowlevel.events.data.processors.CustomPropertyDataRequestProcessor

/**
 * Represents a local data request that will retrieve a custom property from
 * the event's request object if it exists.
 *
 * @param key The key of the custom property to retrieve
 */
case class CustomPropertyDataRequest(key: AnyRef) extends JDIEventDataRequest {
  /**
   * Creates a new JDI event processor based on this data request.
   *
   * @return The new JDI event processor instance
   */
  override def toProcessor: JDIEventProcessor =
    new CustomPropertyDataRequestProcessor(this)
}
