package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.AccessWatchpointEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI access watchpoint event interface.
 */
trait AccessWatchpointEventInfoProfile extends WatchpointEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: AccessWatchpointEvent
}
