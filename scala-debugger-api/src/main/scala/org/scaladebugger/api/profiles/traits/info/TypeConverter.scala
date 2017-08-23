package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.macros.freeze.CanFreeze
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType

import scala.util.Try

trait TypeConverter {
  /**
   * Returns the type as an array type (profile).
   *
   * @return The array type profile wrapping this type
   */
  @CanFreeze(ReturnType.FreezeObject)
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
  @CanFreeze(ReturnType.FreezeObject)
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
  @CanFreeze(ReturnType.FreezeObject)
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
  @CanFreeze(ReturnType.FreezeObject)
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
  @CanFreeze(ReturnType.FreezeObject)
  def toPrimitiveType: PrimitiveTypeInfo

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return Success containing the primitive type profile wrapping this type,
   *         otherwise a failure
   */
  def tryToPrimitiveType: Try[PrimitiveTypeInfo] = Try(toPrimitiveType)
}
