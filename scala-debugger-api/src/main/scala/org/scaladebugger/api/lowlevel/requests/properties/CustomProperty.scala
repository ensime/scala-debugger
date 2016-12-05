package org.scaladebugger.api.lowlevel.requests.properties

import org.scaladebugger.api.lowlevel.requests.properties.processors.CustomPropertyProcessor

/**
 * Represents an argument used set a custom property on the request.
 *
 * @param key The key used for the property
 * @param value The value used for the property
 */
case class CustomProperty(
  key: AnyRef,
  value: AnyRef
) extends JDIRequestProperty with CustomPropertyLike {
  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestPropertyProcessor =
    new CustomPropertyProcessor(this)
}
