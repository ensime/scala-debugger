package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import com.sun.jdi.Value
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents information about a value.
 */
//@Freezable
trait ValueInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ValueInfo

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
  def `type`: TypeInfo

  /**
   * Returns the type information for the value.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryType: Try[TypeInfo] = Try(`type`)

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
  @CannotFreeze
  def toLocalValue: Any

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  @CanFreeze
  def isPrimitive: Boolean

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  @CanFreeze
  def isArray: Boolean

  /**
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  @CanFreeze
  def isClassLoader: Boolean

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  @CanFreeze
  def isClassObject: Boolean

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  @CanFreeze
  def isThreadGroup: Boolean

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  @CanFreeze
  def isThread: Boolean

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  @CanFreeze
  def isObject: Boolean

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  @CanFreeze
  def isString: Boolean

  /**
   * Returns whether or not this value is null.
   *
   * @return True if null, otherwise false
   */
  @CanFreeze
  def isNull: Boolean

  /**
   * Returns whether or not this value is void.
   *
   * @return True if void, otherwise false
   */
  @CanFreeze
  def isVoid: Boolean

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
      else if (this.isArray) "ARRAY TODO"//this.toArrayInfo.toPrettyString
      else if (this.isString) "STRING TODO"//this.toStringInfo.toPrettyString
      else if (this.isClassLoader) "CLASS LOADER TODO"//this.toClassLoaderInfo.toPrettyString
      else if (this.isClassObject) "CLASS OBJECT TODO"//this.toClassObjectInfo.toPrettyString
      else if (this.isThreadGroup) "THREAD GROUP TODO"//this.toThreadGroupInfo.toPrettyString
      else if (this.isThread) "THREAD TODO"//this.toThreadInfo.toPrettyString
      else if (this.isObject) "OBJECT TODO"//this.toObjectInfo.toPrettyString
      else if (this.isPrimitive) "PRIMITIVE TODO"//this.toPrimitiveInfo.toPrettyString
      else "???"
    }.getOrElse("<ERROR>")
  }
}
