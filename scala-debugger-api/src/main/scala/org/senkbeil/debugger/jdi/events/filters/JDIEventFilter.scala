package org.senkbeil.debugger.jdi.events.filters

import org.senkbeil.debugger.jdi.events.processors.JDIEventProcessor

/**
 * Represents a filter for a JDI Event.
 */
trait JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event processor instance
   */
  def toProcessor: JDIEventProcessor
}
