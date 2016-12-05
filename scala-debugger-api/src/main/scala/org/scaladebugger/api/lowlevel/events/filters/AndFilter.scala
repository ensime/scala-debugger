package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.AndFilterProcessor

/**
 * Represents a local filter that will return the result of ANDing multiple
 * filters together.
 *
 * @example AndFilter(Filter1, Filter2) will only pass if both filters pass
 *
 * @param filters The collection of filters to evaluate
 */
case class AndFilter(filters: JDIEventFilter*) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new AndFilterProcessor(this)
}
