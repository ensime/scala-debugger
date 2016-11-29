package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ThreadInfoProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI monitor waited event interface.
 */
trait MonitorWaitedEventInfoProfile extends MonitorEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: MonitorWaitedEvent

  /**
   * Returns the monitor that was waited on.
   *
   * @return The information profile about the monitor object
   */
  override def monitor: ObjectInfoProfile

  /**
   * Returns the thread where the waited event occurred.
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfoProfile

  /**
   * Returns whether or not the wait has timed out or been interrupted.
   *
   * @return True if timed out or interrupted, otherwise false
   */
  def timedout: Boolean
}
