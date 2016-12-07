package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.ClassUnloadEvent

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI class unload event interface.
 */
trait ClassUnloadEventInfo extends EventInfo {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassUnloadEvent

  /**
   * Returns the name of the class being unloaded.
   *
   * @return The fully-qualified class name
   */
  def className: String

  /**
   * Returns the signature of the class being unloaded.
   *
   * @return The JNI-style signature of the class
   */
  def classSignature: String
}
