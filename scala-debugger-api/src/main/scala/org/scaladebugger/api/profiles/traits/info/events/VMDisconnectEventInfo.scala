package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.VMDisconnectEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI vm disconnect event interface.
 */
trait VMDisconnectEventInfo extends EventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: VMDisconnectEvent
}
