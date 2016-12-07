package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Value

import scala.util.Try

/**
 * Represents information about a value.
 */
trait ValueInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ValueInfo

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
  def typeInfo: TypeInfo

  /**
   * Returns the type information for the value.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryTypeInfo: Try[TypeInfo] = Try(typeInfo)

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
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  def isClassLoader: Boolean

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  def isClassObject: Boolean

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  def isThreadGroup: Boolean

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  def isThread: Boolean

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
  def tryToPrimitiveInfo: Try[PrimitiveInfo] = Try(toPrimitiveInfo)

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[AssertionError]
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
  def toArrayInfo: ArrayInfo

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
      else if (this.isClassLoader) this.toClassLoaderInfo.toPrettyString
      else if (this.isClassObject) this.toClassObjectInfo.toPrettyString
      else if (this.isThreadGroup) this.toThreadGroupInfo.toPrettyString
      else if (this.isThread) this.toThreadInfo.toPrettyString
      else if (this.isObject) this.toObjectInfo.toPrettyString
      else if (this.isPrimitive) this.toPrimitiveInfo.toPrettyString
      else "???"
    }.getOrElse("<ERROR>")
  }
}
