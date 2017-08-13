package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Type

import scala.util.Try

/**
 * Represents the interface for retrieving type-based information.
 */
trait TypeInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: TypeInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Type

  /**
   * Represents the readable name for this type.
   *
   * @return The text representation of the type
   */
  def name: String

  /**
   * Represents the JNI-style signature for this type. Primitives have the
   * signature of their corresponding class representation such as "I" for
   * Integer.TYPE.
   *
   * @return The JNI-style signature
   */
  def signature: String

  /**
   * Returns whether or not this type represents a boolean.
   *
   * @return True if a boolean type, otherwise false
   */
  def isBooleanType: Boolean = signature == "Z"

  /**
   * Returns whether or not this type represents a byte.
   *
   * @return True if a byte type, otherwise false
   */
  def isByteType: Boolean = signature == "B"

  /**
   * Returns whether or not this type represents a character.
   *
   * @return True if a char type, otherwise false
   */
  def isCharType: Boolean = signature == "C"

  /**
   * Returns whether or not this type represents a short.
   *
   * @return True if a short type, otherwise false
   */
  def isShortType: Boolean = signature == "S"

  /**
   * Returns whether or not this type represents an integer.
   *
   * @return True if an integer type, otherwise false
   */
  def isIntegerType: Boolean = signature == "I"

  /**
   * Returns whether or not this type represents a long.
   *
   * @return True if a long type, otherwise false
   */
  def isLongType: Boolean = signature == "J"

  /**
   * Returns whether or not this type represents a float.
   *
   * @return True if a float type, otherwise false
   */
  def isFloatType: Boolean = signature == "F"

  /**
   * Returns whether or not this type represents a double.
   *
   * @return True if a double type, otherwise false
   */
  def isDoubleType: Boolean = signature == "D"

  /**
   * Returns whether or not this type represents a string.
   *
   * @return True if a string type, otherwise false
   */
  def isStringType: Boolean = signature == "Ljava/lang/String;"

  /**
   * Returns whether or not this type represents an array type.
   *
   * @return True if an array type, otherwise false
   */
  def isArrayType: Boolean

  /**
   * Returns whether or not this type represents a class type.
   *
   * @return True if a class type, otherwise false
   */
  def isClassType: Boolean

  /**
   * Returns whether or not this type represents an interface type.
   *
   * @return True if an interface type, otherwise false
   */
  def isInterfaceType: Boolean

  /**
   * Returns whether or not this type represents a reference type.
   *
   * @return True if a reference type, otherwise false
   */
  def isReferenceType: Boolean

  /**
   * Returns whether or not this type represents a primitive type.
   *
   * @return True if a primitive type, otherwise false
   */
  def isPrimitiveType: Boolean

  /**
   * Returns whether or not this type is for a value that is null.
   *
   * @return True if representing the type of a null value, otherwise false
   */
  def isNullType: Boolean

  /**
   * Returns the type as an array type (profile).
   *
   * @return The array type profile wrapping this type
   */
  def toArrayType: ArrayTypeInfo

  /**
   * Returns the type as an array type (profile).
   *
   * @return Success containing the array type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToArrayType: Try[ArrayTypeInfo] = Try(toArrayType)

  /**
   * Returns the type as an class type (profile).
   *
   * @return The class type profile wrapping this type
   */
  def toClassType: ClassTypeInfo

  /**
   * Returns the type as an class type (profile).
   *
   * @return Success containing the class type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToClassType: Try[ClassTypeInfo] = Try(toClassType)

  /**
   * Returns the type as an interface type (profile).
   *
   * @return The interface type profile wrapping this type
   */
  def toInterfaceType: InterfaceTypeInfo

  /**
   * Returns the type as an interface type (profile).
   *
   * @return Success containing the interface type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToInterfaceType: Try[InterfaceTypeInfo] = Try(toInterfaceType)

  /**
   * Returns the type as an reference type (profile).
   *
   * @return The reference type profile wrapping this type
   */
  def toReferenceType: ReferenceTypeInfo

  /**
   * Returns the type as an reference type (profile).
   *
   * @return Success containing the reference type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToReferenceType: Try[ReferenceTypeInfo] = Try(toReferenceType)

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return The primitive type profile wrapping this type
   */
  def toPrimitiveType: PrimitiveTypeInfo

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return Success containing the primitive type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToPrimitiveType: Try[PrimitiveTypeInfo] = Try(toPrimitiveType)

  /**
   * Attempts to cast the provided primitive to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return Success containing the resulting value from the transformation,
   *         otherwise a failure
   */
  def tryCastLocal(value: AnyVal): Try[Any] = Try(castLocal(value))

  /**
   * Attempts to cast the provided primitive to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return The resulting value from the transformation
   */
  @throws[CastNotPossibleException]
  def castLocal(value: AnyVal): Any = castLocal(value.toString)

  /**
   * Attempts to cast the provided string to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return The resulting value from the transformation
   */
  def tryCastLocal(value: String): Try[Any] = Try(castLocal(value))

  /**
   * Attempts to cast the provided string to this type, performing any
   * necessary data conversions.
   *
   * @param value The value to transform
   * @return The resulting value from the transformation
   */
  @throws[CastNotPossibleException]
  def castLocal(value: String): Any = {
    def extractFromQuotes(s: String): String =
      "^\"(.*)\"$".r.findFirstMatchIn(s).map(_.group(1)).getOrElse(s)

    // NOTE: Casting whole numbers (integer, long, etc) to double
    //       first to avoid number format issues when given "2.0" as input
    if (isBooleanType)  return value.toBoolean
    if (isByteType)     return value.toDouble.toByte
    if (isCharType)     return value.charAt(0)
    if (isShortType)    return value.toDouble.toShort
    if (isIntegerType)  return value.toDouble.toInt
    if (isLongType)     return value.toDouble.toLong
    if (isFloatType)    return value.toFloat
    if (isDoubleType)   return value.toDouble
    if (isStringType)   return extractFromQuotes(value)

    throw new CastNotPossibleException(value, this)
  }

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = s"Type $name ($signature)"
}
