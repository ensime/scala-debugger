package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Mirror

import scala.util.Try

/**
 * Represents the interface for variable-based interaction.
 */
trait VariableInfo extends CreateInfoProfile with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: VariableInfo

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
   * Returns whether or not this variable was provided with its offset index.
   *
   * @return True if an offset index exists, otherwise false
   */
  def hasOffsetIndex: Boolean = offsetIndex >= 0

  /**
   * Returns the position of this variable in relation to other variables
   * in the same stack frame if local or class if field.
   *
   * @return Non-negative number if provided with an index, otherwise -1
   */
  def offsetIndex: Int

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
  def `type`: TypeInfo

  /**
   * Returns the type information for the variable.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryType: Try[TypeInfo] = Try(`type`)

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
  def tryToValueInfo: Try[ValueInfo] = Try(toValueInfo)

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  def toValueInfo: ValueInfo

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the new remote value, otherwise a failure
   */
  def trySetValue(value: AnyVal): Try[ValueInfo] = Try(setValue(value))

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new remote value
   */
  def setValue(value: AnyVal): ValueInfo =
    setValueFromInfo(createRemotely(value))

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the new remote value, otherwise a failure
   */
  def trySetValue(value: String): Try[ValueInfo] = Try(setValue(value))

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new remote value
   */
  def setValue(value: String): ValueInfo = {
    setValueFromInfo(createRemotely(value))
  }

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return Success containing the variable's value info, otherwise a failure
   */
  def trySetValueFromInfo(valueInfo: ValueInfo): Try[ValueInfo] =
    Try(setValueFromInfo(valueInfo))

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return The info for the variable's new value
   */
  def setValueFromInfo(valueInfo: ValueInfo): ValueInfo

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val name = this.name
    val value = this.tryToValueInfo.map(_.toPrettyString).getOrElse("???")
    s"$name = $value"
  }
}
