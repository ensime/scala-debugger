package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

object JavaValueInfo {
  val DefaultNullTypeName = "null"
}

/**
 * Represents a java implementation of a value profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            value
 * @param infoProducer The producer of info-based profile instances
 * @param _value The reference to the underlying JDI value
 */
class JavaValueInfo(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected[info] val infoProducer: InfoProducer,
  private[info] val _value: Value
) extends ValueInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ValueInfo = {
    infoProducer.toJavaInfo.newValueInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      value = _value
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Value = _value

  /**
   * Returns the type information for the value.
   *
   * @return The profile containing type information
   */
  override def `type`: TypeInfo =
    newTypeProfile(if (!isNull) _value.`type`() else null)

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  override def toLocalValue: Any = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    if (!isNull) _value.value()
    else null
  }

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  override def isPrimitive: Boolean =
    !isNull && (_value.isInstanceOf[PrimitiveValue] || isVoid)

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  override def isArray: Boolean =
    !isNull && _value.isInstanceOf[ArrayReference]

  /**
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  override def isClassLoader: Boolean =
    !isNull && _value.isInstanceOf[ClassLoaderReference]

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  override def isClassObject: Boolean =
    !isNull && _value.isInstanceOf[ClassObjectReference]

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  override def isThread: Boolean =
    !isNull && _value.isInstanceOf[ThreadReference]

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  override def isThreadGroup: Boolean =
    !isNull && _value.isInstanceOf[ThreadGroupReference]

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  override def isObject: Boolean =
    !isNull && _value.isInstanceOf[ObjectReference]

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  override def isString: Boolean =
    !isNull && _value.isInstanceOf[StringReference]

  /**
   * Returns whether or not this value is void.
   *
   * @return True if void, otherwise false
   */
  override def isVoid: Boolean = !isNull && _value.isInstanceOf[VoidValue]

  /**
   * Returns whether or not this value is null.
   *
   * @return True if null, otherwise false
   */
  override def isNull: Boolean = _value == null

  protected def newTypeProfile(_type: Type): TypeInfo =
    infoProducer.newTypeInfo(scalaVirtualMachine, _type)
}
