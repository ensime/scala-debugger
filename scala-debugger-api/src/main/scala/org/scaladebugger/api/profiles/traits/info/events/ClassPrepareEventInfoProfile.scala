package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ClassPrepareEvent
import org.scaladebugger.api.profiles.traits.info.{ReferenceTypeInfoProfile, ThreadInfoProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI class prepare event interface.
 */
trait ClassPrepareEventInfoProfile extends EventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassPrepareEvent

  /**
   * Returns the reference type for which this event occurred.
   *
   * @return The information profile about the reference type
   */
  def referenceType: ReferenceTypeInfoProfile

  /**
   * Returns the thread where this event occurred.
   *
   * @return The information profile about the thread
   */
  def thread: ThreadInfoProfile
}
