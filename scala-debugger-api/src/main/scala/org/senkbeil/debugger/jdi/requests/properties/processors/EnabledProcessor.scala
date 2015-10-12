package org.senkbeil.debugger.jdi.requests.properties.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.properties.EnabledProperty
import org.senkbeil.debugger.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}

/**
 * Represents a processor for the enabled status of a request.
 *
 * @param enabledProperty The enabled property to use when processing
 */
class EnabledProcessor(
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
