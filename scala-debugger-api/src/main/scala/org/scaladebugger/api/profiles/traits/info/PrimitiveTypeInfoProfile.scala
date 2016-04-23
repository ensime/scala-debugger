package org.scaladebugger.api.profiles.traits.info

/**
 * Represents the interface for retrieving primitive type-based information.
 */
trait PrimitiveTypeInfoProfile extends TypeInfoProfile {
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
