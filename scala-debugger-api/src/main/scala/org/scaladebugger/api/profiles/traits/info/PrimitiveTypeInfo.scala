package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import org.scaladebugger.macros.freeze.{CannotFreeze, Freezable}

/**
 * Represents the interface for retrieving primitive type-based information.
 */
//@Freezable
trait PrimitiveTypeInfo extends TypeInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: PrimitiveTypeInfo

  /**
   * Attempts to cast the provided primitive to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return The resulting value from the transformation
   */
  override def castLocal(value: AnyVal): AnyVal =
    super.castLocal(value).asInstanceOf[AnyVal]

  /**
   * Attempts to cast the provided string to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return The resulting value from the transformation
   */
  override def castLocal(value: String): AnyVal =
    super.castLocal(value).asInstanceOf[AnyVal]
}
