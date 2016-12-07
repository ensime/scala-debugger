package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.MethodEntryEvent
import org.scaladebugger.api.profiles.traits.info.MethodInfo

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI method entry event interface.
 */
trait MethodEntryEventInfo extends LocatableEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: MethodEntryEvent

  /**
   * Returns the method that was entered.
   *
   * @return The information profile about the method
   */
  def method: MethodInfo
}
