package org.scaladebugger.api.lowlevel.requests.filters

import org.scaladebugger.api.lowlevel.requests.JDIRequestProcessor
import org.scaladebugger.api.lowlevel.requests.filters.processors.CountFilterProcessor

/**
 * Represents a filter used to restrict events until the specific event has
 * been reached "count" times. In other words, the first count-1 occurrences of
 * the event will not be triggered.
 *
 * Furthermore, the event will only be triggered once, meaning that setting a
 * count filter to 1 will cause the event to only fire once (on the first time
 * the event is reached).
 *
 * @param count The event will not be triggered the first "count - 1" times
 */
case class CountFilter(count: Int) extends JDIRequestFilter {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestFilterProcessor =
    new CountFilterProcessor(this)
}
