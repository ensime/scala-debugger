package org.scaladebugger.api.profiles.traits.info


import com.sun.jdi.Value
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.{Failure, Success, Try}

/**
 * Represents information about a primitive value.
 */
//@Freezable
trait PrimitiveInfo extends ValueInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: PrimitiveInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: Value

  /**
   * Returns the type information for the value.
   *
   * @return The profile containing type information
   */
  @CanFreeze(ReturnType.FreezeObject)
  override def `type`: PrimitiveTypeInfo

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return Success containing the value as a local instance,
   *         otherwise a failure
   */
  override def tryToLocalValue: Try[AnyVal] = Try(toLocalValue)

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  @CanFreeze
  override def toLocalValue: AnyVal

  /**
   * Returns whether or not this primitive is a boolean.
   *
   * @return True if the primitive is a boolean, otherwise false
   */
  @CanFreeze
  def isBoolean: Boolean

  /**
   * Returns whether or not this primitive is a byte.
   *
   * @return True if the primitive is a byte, otherwise false
   */
  @CanFreeze
  def isByte: Boolean

  /**
   * Returns whether or not this primitive is a char.
   *
   * @return True if the primitive is a char, otherwise false
   */
  @CanFreeze
  def isChar: Boolean

  /**
   * Returns whether or not this primitive is a double.
   *
   * @return True if the primitive is a double, otherwise false
   */
  @CanFreeze
  def isDouble: Boolean

  /**
   * Returns whether or not this primitive is a float.
   *
   * @return True if the primitive is a float, otherwise false
   */
  @CanFreeze
  def isFloat: Boolean

  /**
   * Returns whether or not this primitive is a integer.
   *
   * @return True if the primitive is a integer, otherwise false
   */
  @CanFreeze
  def isInteger: Boolean

  /**
   * Returns whether or not this primitive is a long.
   *
   * @return True if the primitive is a long, otherwise false
   */
  @CanFreeze
  def isLong: Boolean

  /**
   * Returns whether or not this primitive is a short.
   *
   * @return True if the primitive is a short, otherwise false
   */
  @CanFreeze
  def isShort: Boolean

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = this.tryToLocalValue match {
    case Success(value) =>
      if (this.isChar) s"'$value'"
      else value.toString

    case Failure(_) =>
      "<ERROR>"
  }
}
