package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.MonitorWaitEvent
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ThreadInfoProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI monitor wait event interface.
 */
trait MonitorWaitEventInfoProfile extends MonitorEventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: MonitorWaitEvent

  /**
   * Returns the monitor that will wait on.
   *
   * @return The information profile about the monitor object
   */
  override def monitor: ObjectInfoProfile

  /**
   * Returns the thread where the event occurred.
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfoProfile

  /**
   * Returns the number of milliseconds the thread will wait on the monitor.
   *
   * @return The length of time as milliseconds
   */
  def timeout: Long
}
