package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

import scala.util.Try

trait FrozenValueInfoLike extends ValueInfo with FrozenCommonInfo {
  override val isNull: Boolean
  override val isVoid: Boolean
  protected val _localValue: Try[Any]
  protected val _arrayInfo: Try[ArrayInfo]
  protected val _stringInfo: Try[StringInfo]
  protected val _objectInfo: Try[ObjectInfo]
  protected val _threadInfo: Try[ThreadInfo]
  protected val _threadGroupInfo: Try[ThreadGroupInfo]
  protected val _classObjectInfo: Try[ClassObjectInfo]
  protected val _classLoaderInfo: Try[ClassLoaderInfo]
  protected val _primitiveInfo: Try[PrimitiveInfo]

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  override def isPrimitive: Boolean = _primitiveInfo.isSuccess

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  override def isArray: Boolean = _arrayInfo.isSuccess

  /**
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  override def isClassLoader: Boolean = _classLoaderInfo.isSuccess

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  override def isClassObject: Boolean = _classObjectInfo.isSuccess

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  override def isThreadGroup: Boolean = _threadGroupInfo.isSuccess

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  override def isThread: Boolean = _threadInfo.isSuccess

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  override def isObject: Boolean = _objectInfo.isSuccess

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  override def isString: Boolean = _stringInfo.isSuccess

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  @throws[FrozenException]
  override def toLocalValue: Any = _localValue.get

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[FrozenException]
  override def toPrimitiveInfo: PrimitiveInfo = _primitiveInfo.get

  /**
   * Returns the value as a class loader (profile).
   *
   * @return The class loader profile wrapping this value
   */
  @throws[FrozenException]
  override def toClassLoaderInfo: ClassLoaderInfo = _classLoaderInfo.get

  /**
   * Returns the value as a class object (profile).
   *
   * @return The class object profile wrapping this value
   */
  @throws[FrozenException]
  override def toClassObjectInfo: ClassObjectInfo = _classObjectInfo.get

  /**
   * Returns the value as a thread group (profile).
   *
   * @return The thread group profile wrapping this value
   */
  @throws[FrozenException]
  override def toThreadGroupInfo: ThreadGroupInfo = _threadGroupInfo.get

  /**
   * Returns the value as a thread (profile).
   *
   * @return The thread profile wrapping this value
   */
  @throws[FrozenException]
  override def toThreadInfo: ThreadInfo = _threadInfo.get

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[FrozenException]
  override def toObjectInfo: ObjectInfo = _objectInfo.get

  /**
   * Returns the value as an string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[FrozenException]
  override def toStringInfo: StringInfo = _stringInfo.get

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[FrozenException]
  override def toArrayInfo: ArrayInfo = _arrayInfo.get
}
