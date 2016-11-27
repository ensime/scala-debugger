package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.MethodExitEvent
import org.scaladebugger.api.profiles.traits.info.{MethodInfoProfile, ValueInfoProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI method exit event interface.
 */
trait MethodExitEventInfoProfile extends LocatableEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: MethodExitEvent

  /**
   * Returns the method that was exited.
   *
   * @return The information profile about the method
   */
  def method: MethodInfoProfile

  /**
   * Returns the value that the method will return.
   *
   * @return The information profile about the value
   */
  def returnValue: ValueInfoProfile
}
