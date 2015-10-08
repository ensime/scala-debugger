package org.senkbeil.debugger.jdi.requests.properties

import org.senkbeil.debugger.jdi.requests.processors.{CustomPropertyProcessor, JDIRequestProcessor}

/**
 * Represents an argument used set a custom property on the request.
 *
 * @param key The key used for the property
 * @param value The value used for the property
 */
case class CustomProperty(key: AnyRef, value: AnyRef)
  extends JDIRequestProperty
{
  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestProcessor =
    new CustomPropertyProcessor(this)
}
