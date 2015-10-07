package org.senkbeil.debugger.jdi.events

import org.senkbeil.debugger.jdi.events.processors.JDIEventProcessor

/**
 * Represents an argument for a JDI Event.
 */
trait JDIEventArgument {
  /**
   * Creates a new JDI event processor based on this argument.
   *
   * @return The new JDI event processor instance
   */
  def toProcessor: JDIEventProcessor
}
