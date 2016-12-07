package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.BreakpointEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI breakpoint event interface.
 */
trait BreakpointEventInfo extends LocatableEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: BreakpointEvent
}
