package org.scaladebugger.api.lowlevel.requests.properties

import org.scaladebugger.api.lowlevel.requests.properties.processors.EnabledPropertyProcessor

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
  override def toProcessor: JDIRequestPropertyProcessor =
    new EnabledPropertyProcessor(this)
}
