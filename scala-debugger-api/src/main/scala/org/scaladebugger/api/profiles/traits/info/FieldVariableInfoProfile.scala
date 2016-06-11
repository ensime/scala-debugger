package org.scaladebugger.api.profiles.traits.info

//import acyclic.file

/**
 * Represents the interface for variable-based interaction with field-specific
 * information.
 */
trait FieldVariableInfoProfile
  extends VariableInfoProfile with CreateInfoProfile with CommonInfoProfile
{
  /**
   * Returns the parent that contains this field.
   *
   * @return The reference type information (if a static field) or object
   *         information (if a non-static field)
   */
  def parent: Either[ObjectInfoProfile, ReferenceTypeInfoProfile]
}
