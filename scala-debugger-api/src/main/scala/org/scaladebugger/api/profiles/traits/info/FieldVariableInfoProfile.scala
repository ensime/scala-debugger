package org.scaladebugger.api.profiles.traits.info

import scala.util.Try

//import acyclic.file

/**
 * Represents the interface for variable-based interaction with field-specific
 * information.
 */
trait FieldVariableInfoProfile
  extends VariableInfoProfile with CreateInfoProfile with CommonInfoProfile
{
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: FieldVariableInfoProfile

  /**
   * Returns the parent that contains this field.
   *
   * @return The reference type information (if a static field) or object
   *         information (if a non-static field)
   */
  def parent: Either[ObjectInfoProfile, ReferenceTypeInfoProfile]

  /**
   * Returns the type where this field was declared.
   *
   * @return The reference type information that declared this field
   */
  def declaringTypeInfo: ReferenceTypeInfoProfile

  /**
   * Returns the type where this field was declared.
   *
   * @return The reference type information that declared this field
   */
  def tryDeclaringTypeInfo: Try[ReferenceTypeInfoProfile] =
    Try(declaringTypeInfo)
}
