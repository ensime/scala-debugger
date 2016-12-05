package org.scaladebugger.api.lowlevel.requests.properties.processors

import com.sun.jdi.request._
import org.scaladebugger.api.lowlevel.requests.properties.{JDIRequestPropertyProcessor, JDIRequestProperty, EnabledProperty}

/**
 * Represents a processor for the enabled status of a request.
 *
 * @param enabledProperty The enabled property to use when processing
 */
class EnabledPropertyProcessor(
  val enabledProperty: EnabledProperty
) extends JDIRequestPropertyProcessor {
  private val value = enabledProperty.value

  /**
   * Processes the provided event request with the property logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    eventRequest.setEnabled(value)

    eventRequest
  }

  override val argument: JDIRequestProperty = enabledProperty
}
