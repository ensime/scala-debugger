package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.Value

import scala.util.Try

/**
 * Represents information about a value.
 */
trait ValueInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Value

  /**
   * Returns the type information for the value.
   *
   * @return The profile containing type information
   */
  def typeInfo: TypeInfoProfile

  /**
   * Returns the type information for the value.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryTypeInfo: Try[TypeInfoProfile] = Try(typeInfo)

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return Success containing the value as a local instance,
   *         otherwise a failure
   */
  def tryToLocalValue: Try[Any] = Try(toLocalValue)

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  def toLocalValue: Any

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  def isPrimitive: Boolean

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  def isArray: Boolean

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  def isObject: Boolean

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  def isString: Boolean

  /**
   * Returns whether or not this value is null.
   *
   * @return True if null, otherwise false
   */
  def isNull: Boolean

  /**
   * Returns whether or not this value is void.
   *
   * @return True if void, otherwise false
   */
  def isVoid: Boolean

  /**
   * Returns the value as a primitive (profile).
   *
   * @return Success containing the primitive profile wrapping this value,
   *         otherwise a failure
   */
  def tryToPrimitiveInfo: Try[PrimitiveInfoProfile] = Try(toPrimitiveInfo)

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[AssertionError]
  def toPrimitiveInfo: PrimitiveInfoProfile

  /**
   * Returns the value as an object (profile).
   *
   * @return Success containing the object profile wrapping this value,
   *         otherwise a failure
   */
  def tryToObjectInfo: Try[ObjectInfoProfile] = Try(toObjectInfo)

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[AssertionError]
  def toObjectInfo: ObjectInfoProfile

  /**
   * Returns the value as a string (profile).
   *
   * @return Success containing the string profile wrapping this value,
   *         otherwise a failure
   */
  def tryToStringInfo: Try[StringInfoProfile] = Try(toStringInfo)

  /**
   * Returns the value as an string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[AssertionError]
  def toStringInfo: StringInfoProfile

  /**
   * Returns the value as an array (profile).
   *
   * @return Success containing the array profile wrapping this value,
   *         otherwise a failure
   */
  def tryToArrayInfo: Try[ArrayInfoProfile] = Try(toArrayInfo)

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[AssertionError]
  def toArrayInfo: ArrayInfoProfile

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    Try {
      if (this.isNull) "null"
      else if (this.isVoid) "void"
      else if (this.isArray) this.toArrayInfo.toPrettyString
      else if (this.isString) this.toStringInfo.toPrettyString
      else if (this.isObject) this.toObjectInfo.toPrettyString
      else if (this.isPrimitive) this.toPrimitiveInfo.toPrettyString
      else "???"
    }.getOrElse("<ERROR>")
  }
}
