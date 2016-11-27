package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.VMStartEvent
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI vm start event interface.
 */
trait VMStartEventInfoProfile extends EventInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: VMStartEvent

  /**
   * Returns the initial thread of the VM that started.
   *
   * @return The information profile about the thread
   */
  def thread: ThreadInfoProfile
}
