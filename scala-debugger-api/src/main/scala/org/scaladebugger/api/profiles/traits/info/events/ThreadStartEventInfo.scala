package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ThreadStartEvent
import org.scaladebugger.api.profiles.traits.info.ThreadInfo

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI thread start event interface.
 */
trait ThreadStartEventInfo extends EventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadStartEvent

  /**
   * Returns the thread which started.
   *
   * @return The information profile about the thread
   */
  def thread: ThreadInfo
}
