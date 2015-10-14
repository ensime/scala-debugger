package org.senkbeil.debugger.api.jdi.events.filters

import org.senkbeil.debugger.api.jdi.events.JDIEventProcessor
import org.senkbeil.debugger.api.jdi.events.filters.processors.CustomPropertyFilterProcessor

/**
 * Represents a local filter that will result in ignoring any incoming event if
 * it does have a matching custom property.
 *
 * @example CustomPropertyFilter("test", 33) will only allow events that have
 *          a request with a custom property whose key is "test" and value for
 *          that key is 33
 *
 * @param key The key of the property to match
 * @param value The value of the property to match
 */
case class CustomPropertyFilter(
  key: AnyRef,
  value: AnyRef
) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event processor instance
   */
  override def toProcessor: JDIEventProcessor =
    new CustomPropertyFilterProcessor(this)
}
