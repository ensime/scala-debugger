package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.VMDeathEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI vm death event interface.
 */
trait VMDeathEventInfo extends EventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: VMDeathEvent
}
