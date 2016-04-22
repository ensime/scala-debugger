package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.Mirror

import scala.util.Try

/**
 * Represents the interface for variable-based interaction.
 */
trait VariableInfoProfile extends CreateInfoProfile with CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Mirror

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  def name: String

  /**
   * Returns the name of the type representing the variable.
   *
   * @return The type name as a string
   */
  def typeName: String

  /**
   * Returns the type information for the variable.
   *
   * @return The profile containing type information
   */
  def typeInfo: TypeInfoProfile

  /**rsenkbeil3
   * Returns the type information for the variable.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryTypeInfo: Try[TypeInfoProfile] = Try(typeInfo)

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  def isField: Boolean

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  def isLocal: Boolean

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  def isArgument: Boolean

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return Success containing the profile representing the value, otherwise
   *         a failure
   */
  def tryToValue: Try[ValueInfoProfile] = Try(toValue)

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  def toValue: ValueInfoProfile

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the new remote value, otherwise a failure
   */
  def trySetValue(value: AnyVal): Try[ValueInfoProfile] = Try(setValue(value))

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new remote value
   */
  def setValue(value: AnyVal): ValueInfoProfile =
    setValueFromInfo(createRemotely(value))

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the new remote value, otherwise a failure
   */
  def trySetValue(value: String): Try[ValueInfoProfile] = Try(setValue(value))

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new remote value
   */
  def setValue(value: String): ValueInfoProfile = {
    setValueFromInfo(createRemotely(value))
  }

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return Success containing the variable's value info, otherwise a failure
   */
  def trySetValueFromInfo(valueInfo: ValueInfoProfile): Try[ValueInfoProfile] =
    Try(setValueFromInfo(valueInfo))

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return The info for the variable's new value
   */
  def setValueFromInfo(valueInfo: ValueInfoProfile): ValueInfoProfile

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val name = this.name
    val value = this.tryToValue.map(_.toPrettyString).getOrElse("???")
    s"$name = $value"
  }
}
