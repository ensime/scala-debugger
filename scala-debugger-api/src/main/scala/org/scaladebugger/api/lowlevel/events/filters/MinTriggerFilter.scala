package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.MinTriggerFilterProcessor

/**
 * Represents a local filter that will result in ignoring any incoming event
 * until N successful events have been reported.
 *
 * @example MinTriggerFilter(count = 3) will ignore event 1, 2, and 3; allowing
 *          all subsequent events
 *
 * @param count The total number of events to ignore before allowing all
 *              subsequent events
 */
case class MinTriggerFilter(count: Int) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new MinTriggerFilterProcessor(this)
}
