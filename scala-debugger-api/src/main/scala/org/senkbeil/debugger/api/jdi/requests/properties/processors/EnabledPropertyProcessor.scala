package org.senkbeil.debugger.api.jdi.requests.properties.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.api.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.senkbeil.debugger.api.jdi.requests.properties.EnabledProperty

/**
 * Represents a processor for the enabled status of a request.
 *
 * @param enabledProperty The enabled property to use when processing
 */
class EnabledPropertyProcessor(
  val enabledProperty: EnabledProperty
) extends JDIRequestProcessor {
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

  override val argument: JDIRequestArgument = enabledProperty
}
