package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.macros.freeze.CanFreeze
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType

import scala.util.Try

trait ValueConverter {
  /**
   * Returns the value as a primitive (profile).
   *
   * @return Success containing the primitive profile wrapping this value,
   *         otherwise a failure
   */
  def tryToPrimitiveInfo: Try[PrimitiveInfo] = Try(toPrimitiveInfo)

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toPrimitiveInfo: PrimitiveInfo

  /**
   * Returns the value as a class loader (profile).
   *
   * @return Success containing the class loader profile wrapping this value,
   *         otherwise a failure
   */
  def tryToClassLoaderInfo: Try[ClassLoaderInfo] = Try(toClassLoaderInfo)

  /**
   * Returns the value as a class loader (profile).
   *
   * @return The class loader profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toClassLoaderInfo: ClassLoaderInfo

  /**
   * Returns the value as a class object (profile).
   *
   * @return Success containing the class object profile wrapping this value,
   *         otherwise a failure
   */
  def tryToClassObjectInfo: Try[ClassObjectInfo] = Try(toClassObjectInfo)

  /**
   * Returns the value as a class object (profile).
   *
   * @return The class object profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toClassObjectInfo: ClassObjectInfo

  /**
   * Returns the value as a thread group (profile).
   *
   * @return Success containing the thread group profile wrapping this value,
   *         otherwise a failure
   */
  def tryToThreadGroupInfo: Try[ThreadGroupInfo] = Try(toThreadGroupInfo)

  /**
   * Returns the value as a thread group (profile).
   *
   * @return The thread group profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toThreadGroupInfo: ThreadGroupInfo

  /**
   * Returns the value as a thread (profile).
   *
   * @return Success containing the thread profile wrapping this value,
   *         otherwise a failure
   */
  def tryToThreadInfo: Try[ThreadInfo] = Try(toThreadInfo)

  /**
   * Returns the value as a thread (profile).
   *
   * @return The thread profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toThreadInfo: ThreadInfo

  /**
   * Returns the value as an object (profile).
   *
   * @return Success containing the object profile wrapping this value,
   *         otherwise a failure
   */
  def tryToObjectInfo: Try[ObjectInfo] = Try(toObjectInfo)

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toObjectInfo: ObjectInfo

  /**
   * Returns the value as a string (profile).
   *
   * @return Success containing the string profile wrapping this value,
   *         otherwise a failure
   */
  def tryToStringInfo: Try[StringInfo] = Try(toStringInfo)

  /**
   * Returns the value as an string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toStringInfo: StringInfo

  /**
   * Returns the value as an array (profile).
   *
   * @return Success containing the array profile wrapping this value,
   *         otherwise a failure
   */
  def tryToArrayInfo: Try[ArrayInfo] = Try(toArrayInfo)

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[AssertionError]
  @CanFreeze(ReturnType.FreezeObject)
  def toArrayInfo: ArrayInfo
}
