package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

trait FrozenValueInfoLike extends ValueInfo with FrozenCommonInfo {
  override val isNull: Boolean
  override val isVoid: Boolean
  protected val localValue: Either[FrozenException, Any]
  protected val arrayInfo: Either[FrozenException, ArrayInfo]
  protected val stringInfo: Either[FrozenException, StringInfo]
  protected val objectInfo: Either[FrozenException, ObjectInfo]
  protected val threadInfo: Either[FrozenException, ThreadInfo]
  protected val threadGroupInfo: Either[FrozenException, ThreadGroupInfo]
  protected val classObjectInfo: Either[FrozenException, ClassObjectInfo]
  protected val classLoaderInfo: Either[FrozenException, ClassLoaderInfo]
  protected val primitiveInfo: Either[FrozenException, PrimitiveInfo]

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  override def isPrimitive: Boolean = primitiveInfo.isRight

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  override def isArray: Boolean = arrayInfo.isRight

  /**
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  override def isClassLoader: Boolean = classLoaderInfo.isRight

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  override def isClassObject: Boolean = classObjectInfo.isRight

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  override def isThreadGroup: Boolean = threadGroupInfo.isRight

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  override def isThread: Boolean = threadInfo.isRight

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  override def isObject: Boolean = objectInfo.isRight

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  override def isString: Boolean = stringInfo.isRight

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  @throws[FrozenException]
  override def toLocalValue: Any = localValue match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[FrozenException]
  override def toPrimitiveInfo: PrimitiveInfo = primitiveInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as a class loader (profile).
   *
   * @return The class loader profile wrapping this value
   */
  @throws[FrozenException]
  override def toClassLoaderInfo: ClassLoaderInfo = classLoaderInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as a class object (profile).
   *
   * @return The class object profile wrapping this value
   */
  @throws[FrozenException]
  override def toClassObjectInfo: ClassObjectInfo = classObjectInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as a thread group (profile).
   *
   * @return The thread group profile wrapping this value
   */
  @throws[FrozenException]
  override def toThreadGroupInfo: ThreadGroupInfo = threadGroupInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as a thread (profile).
   *
   * @return The thread profile wrapping this value
   */
  @throws[FrozenException]
  override def toThreadInfo: ThreadInfo = threadInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[FrozenException]
  override def toObjectInfo: ObjectInfo = objectInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as an string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[FrozenException]
  override def toStringInfo: StringInfo = stringInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[FrozenException]
  override def toArrayInfo: ArrayInfo = arrayInfo match {
    case Left(e) => throw e
    case Right(v) => v
  }
}
