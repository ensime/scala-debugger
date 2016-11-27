package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ModificationWatchpointEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI modification watchpoint event interface.
 */
trait ModificationWatchpointEventInfoProfile extends WatchpointEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ModificationWatchpointEvent
}
