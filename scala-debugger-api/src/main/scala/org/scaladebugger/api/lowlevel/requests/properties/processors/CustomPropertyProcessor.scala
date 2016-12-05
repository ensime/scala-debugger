package org.scaladebugger.api.lowlevel.requests.properties.processors

import com.sun.jdi.request._
import org.scaladebugger.api.lowlevel.requests.properties.{CustomPropertyLike, JDIRequestPropertyProcessor, JDIRequestProperty, CustomProperty}

/**
 * Represents a processor for the custom property of a request.
 *
 * @param customProperty The custom property to use when processing
 */
class CustomPropertyProcessor(
  val customProperty: JDIRequestProperty with CustomPropertyLike
) extends JDIRequestPropertyProcessor {
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

  override val argument: JDIRequestProperty = customProperty
}
