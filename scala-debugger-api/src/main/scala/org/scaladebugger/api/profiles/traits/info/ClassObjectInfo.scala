package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import com.sun.jdi.ClassObjectReference
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

/**
 * Represents the interface for "class object"-based interaction.
 */
//@Freezable
trait ClassObjectInfo extends ObjectInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ClassObjectInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: ClassObjectReference

  /**
   * Retrieves the reference type information corresponding to this class
   * object.
   *
   * @return The reference type information
   */
  @CanFreeze(ReturnType.FreezeObject)
  def reflectedType: ReferenceTypeInfo
}
