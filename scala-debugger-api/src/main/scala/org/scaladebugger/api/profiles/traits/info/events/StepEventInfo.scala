package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.StepEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI step event interface.
 */
trait StepEventInfo extends LocatableEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StepEvent
}
