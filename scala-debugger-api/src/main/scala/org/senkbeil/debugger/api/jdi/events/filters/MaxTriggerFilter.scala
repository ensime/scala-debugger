package org.senkbeil.debugger.api.jdi.events.filters

import org.senkbeil.debugger.api.jdi.events.JDIEventProcessor
import org.senkbeil.debugger.api.jdi.events.filters.processors.MaxTriggerFilterProcessor

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
   * @return The new JDI event processor instance
   */
  override def toProcessor: JDIEventProcessor = new MaxTriggerFilterProcessor(this)
}
