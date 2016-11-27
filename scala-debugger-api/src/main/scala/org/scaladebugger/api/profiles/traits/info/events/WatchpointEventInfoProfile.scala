package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.WatchpointEvent
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI watchpoint event interface.
 */
trait WatchpointEventInfoProfile extends LocatableEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: WatchpointEvent

  /**
   * Returns the field that is about to be accessed/modified.
   *
   * @return The information profile about the field
   */
  def field: FieldVariableInfoProfile

  /**
   * Returns the object whose field is about to be accessed/modified.
   *
   * @return Some information profile about the object if the field is from
   *         an instance of an object, otherwise None if the field is
   *         static
   */
  def `object`: Option[ObjectInfoProfile]

  /**
   * Returns the value of the field that is about to be accessed/modified.
   *
   * @return The information profile about the value
   */
  def currentValue: ValueInfoProfile
}
