package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ModificationWatchpointEvent
import org.scaladebugger.api.profiles.traits.info.ValueInfo

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI modification watchpoint event interface.
 */
trait ModificationWatchpointEventInfo extends WatchpointEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ModificationWatchpointEvent

  /**
   * Returns the value to be assigned to the variable being modified.
   *
   * @return The information profile about the value
   */
  def valueToBe: ValueInfo
}
