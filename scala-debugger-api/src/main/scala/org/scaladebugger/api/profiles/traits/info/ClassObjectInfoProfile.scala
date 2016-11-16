package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ClassObjectReference

/**
 * Represents the interface for "class object"-based interaction.
 */
trait ClassObjectInfoProfile extends ObjectInfoProfile with CommonInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ClassObjectInfoProfile

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassObjectReference

  /**
   * Retrieves the reference type information corresponding to this class
   * object.
   *
   * @return The reference type information
   */
  def reflectedType: ReferenceTypeInfoProfile
}
