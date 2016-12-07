package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.profiles.traits.info.{LocationInfo, ObjectInfo}

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI exception event interface.
 */
trait ExceptionEventInfo extends LocatableEventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ExceptionEvent

  /**
   * Returns the location where the exception will be caught.
   *
   * @return Some information profile about the location if the exception is
   *         a caught exception, otherwise None if it is uncaught
   */
  def catchLocation: Option[LocationInfo]

  /**
   * Returns the thrown exception object.
   *
   * @return The information profile about the exception object
   */
  def exception: ObjectInfo
}
