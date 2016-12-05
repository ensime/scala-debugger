package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.NotFilterProcessor

/**
 * Represents a local filter that will return the result of negating a filter.
 *
 * @example NotFilter(Filter1) will pass if Filter1 does not pass
 *
 * @param filter The filter to negate
 */
case class NotFilter(filter: JDIEventFilter) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new NotFilterProcessor(this)
}
