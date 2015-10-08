package org.senkbeil.debugger.jdi.requests.properties

import org.senkbeil.debugger.jdi.requests.JDIRequestProcessor
import org.senkbeil.debugger.jdi.requests.processors.EnabledProcessor

/**
 * Represents an argument used set the enabled status of the request.
 *
 * @param value The value to use for the enabled status of the request
 */
case class EnabledProperty(value: Boolean) extends JDIRequestProperty {
  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestProcessor = new EnabledProcessor(this)
}
