package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

/** Represents a frozen instance of some type information on a remote JVM. */
trait FrozenTypeInfoLike extends TypeInfo with FrozenCommonInfo {
  protected val arrayType: Either[FrozenException, ArrayTypeInfo]
  protected val classType: Either[FrozenException, ClassTypeInfo]
  protected val interfaceType: Either[FrozenException, InterfaceTypeInfo]
  protected val referenceType: Either[FrozenException, ReferenceTypeInfo]
  protected val primitiveType: Either[FrozenException, PrimitiveTypeInfo]

  /**
   * Returns whether or not this type represents an array type.
   *
   * @return True if an array type, otherwise false
   */
  override def isArrayType: Boolean = arrayType.isRight

  /**
   * Returns whether or not this type represents a class type.
   *
   * @return True if a class type, otherwise false
   */
  override def isClassType: Boolean = classType.isRight

  /**
   * Returns whether or not this type represents an interface type.
   *
   * @return True if an interface type, otherwise false
   */
  override def isInterfaceType: Boolean = interfaceType.isRight

  /**
   * Returns whether or not this type represents a reference type.
   *
   * @return True if a reference type, otherwise false
   */
  override def isReferenceType: Boolean = referenceType.isRight

  /**
   * Returns whether or not this type represents a primitive type.
   *
   * @return True if a primitive type, otherwise false
   */
  override def isPrimitiveType: Boolean = primitiveType.isRight

  /**
   * Returns whether or not this type is for a value that is null.
   *
   * @return True if representing the type of a null value, otherwise false
   */
  override def isNullType: Boolean = !(
    isArrayType || isClassType || isInterfaceType ||
    isReferenceType || isPrimitiveType
  )

  /**
   * Returns the type as an array type (profile).
   *
   * @return The array type profile wrapping this type
   */
  @throws[FrozenException]
  override def toArrayType: ArrayTypeInfo = arrayType match {
    case Left(e) => throw e
    case Right(t) => t
  }

  /**
   * Returns the type as an class type (profile).
   *
   * @return The class type profile wrapping this type
   */
  @throws[FrozenException]
  override def toClassType: ClassTypeInfo = classType match {
    case Left(e) => throw e
    case Right(t) => t
  }

  /**
   * Returns the type as an interface type (profile).
   *
   * @return The interface type profile wrapping this type
   */
  @throws[FrozenException]
  override def toInterfaceType: InterfaceTypeInfo = interfaceType match {
    case Left(e) => throw e
    case Right(t) => t
  }

  /**
   * Returns the type as an reference type (profile).
   *
   * @return The reference type profile wrapping this type
   */
  @throws[FrozenException]
  override def toReferenceType: ReferenceTypeInfo = referenceType match {
    case Left(e) => throw e
    case Right(t) => t
  }

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return The primitive type profile wrapping this type
   */
  @throws[FrozenException]
  override def toPrimitiveType: PrimitiveTypeInfo = primitiveType match {
    case Left(e) => throw e
    case Right(t) => t
  }
}
