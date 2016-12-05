package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.OrFilterProcessor

/**
 * Represents a local filter that will return the result of ORing multiple
 * filters together.
 *
 * @example OrFilter(Filter1, Filter2) will pass if either filter passes
 *
 * @param filters The collection of filters to evaluate
 */
case class OrFilter(filters: JDIEventFilter*) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new OrFilterProcessor(this)
}
