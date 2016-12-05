package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.MaxTriggerFilterProcessor

/**
 * Represents a local filter that will result in ignoring any incoming event
 * after N successful events have been reported.
 *
 * @example MaxTriggerFilter(count = 3) will allow event 1, 2, and 3; ignoring
 *          all subsequent events
 *
 * @param count The total number of events to allow before ignoring all
 *              subsequent events
 */
case class MaxTriggerFilter(count: Int) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new MaxTriggerFilterProcessor(this)
}
