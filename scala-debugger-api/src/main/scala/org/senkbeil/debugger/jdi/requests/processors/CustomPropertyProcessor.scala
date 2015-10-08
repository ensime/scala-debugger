package org.senkbeil.debugger.jdi.requests.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.{JDIRequestProcessor, JDIRequestArgument}
import org.senkbeil.debugger.jdi.requests.properties.CustomProperty

/**
 * Represents a processor for the custom property of a request.
 *
 * @param customProperty The custom property to use when processing
 */
class CustomPropertyProcessor(
  val customProperty: CustomProperty
) extends JDIRequestProcessor {
  private val key = customProperty.key
  private val value = customProperty.value

  /**
   * Processes the provided event request with the property logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    eventRequest.putProperty(key, value)

    eventRequest
  }

  override val argument: JDIRequestArgument = customProperty
}
