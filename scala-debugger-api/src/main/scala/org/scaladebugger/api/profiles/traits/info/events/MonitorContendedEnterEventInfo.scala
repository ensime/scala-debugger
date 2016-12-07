package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, ThreadInfo}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI monitor contended enter event interface.
 */
trait MonitorContendedEnterEventInfo extends MonitorEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: MonitorContendedEnterEvent

  /**
   * Returns the monitor that was entered.
   *
   * @return The information profile about the monitor object
   */
  override def monitor: ObjectInfo

  /**
   * Returns the thread where this event occurred.
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfo
}
