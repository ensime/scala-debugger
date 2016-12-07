package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.LocatableEvent
import org.scaladebugger.api.profiles.traits.info.{LocationInfo, ThreadInfo}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI locatable event interface.
 */
trait LocatableEventInfo extends EventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: LocatableEvent

  /**
   * Returns the thread associated with this event.
   *
   * @return The information profile about the thread
   */
  def thread: ThreadInfo

  /**
   * Returns the location associated with this event.
   *
   * @return The information profile about the location
   */
  def location: LocationInfo
}
