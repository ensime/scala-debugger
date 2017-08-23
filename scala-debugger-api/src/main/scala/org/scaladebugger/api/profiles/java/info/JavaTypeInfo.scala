package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayTypeInfo, ReferenceTypeInfo, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import JavaTypeInfo._

object JavaTypeInfo {
  val DefaultNullTypeName = "null"
  val DefaultNullTypeSignature = "null"
}

/**
 * Represents a java implementation of a type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param infoProducer The producer of info-based profile instances
 * @param _type The reference to the underlying JDI type
 */
class JavaTypeInfo(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected[info] val infoProducer: InfoProducer,
  private[info] val _type: Type
) extends TypeInfo {
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
  override def toJavaInfo: TypeInfo = {
    infoProducer.toJavaInfo.newTypeInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      _type = _type
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Type = _type

  /**
   * Represents the readable name for this type.
   *
   * @return The text representation of the type
   */
  override def name: String =
    if (!isNullType) _type.name() else DefaultNullTypeName

  /**
   * Represents the JNI-style signature for this type. Primitives have the
   * signature of their corresponding class representation such as "I" for
   * Integer.TYPE.
   *
   * @return The JNI-style signature
   */
  override def signature: String =
    if (!isNullType) _type.signature() else DefaultNullTypeSignature

  /**
   * Returns whether or not this type represents an array type.
   *
   * @return True if an array type, otherwise false
   */
  override def isArrayType: Boolean =
    !isNullType && _type.isInstanceOf[ArrayType]

  /**
   * Returns whether or not this type represents a class type.
   *
   * @return True if a class type, otherwise false
   */
  override def isClassType: Boolean =
    !isNullType && _type.isInstanceOf[ClassType]

  /**
   * Returns whether or not this type represents an interface type.
   *
   * @return True if an interface type, otherwise false
   */
  override def isInterfaceType: Boolean =
    !isNullType && _type.isInstanceOf[InterfaceType]

  /**
   * Returns whether or not this type represents a reference type.
   *
   * @return True if a reference type, otherwise false
   */
  override def isReferenceType: Boolean =
    !isNullType && _type.isInstanceOf[ReferenceType]

  /**
   * Returns whether or not this type represents a primitive type.
   *
   * @return True if a primitive type, otherwise false
   */
  override def isPrimitiveType: Boolean =
    _type.isInstanceOf[PrimitiveType] || _type.isInstanceOf[VoidType]

  /**
   * Returns whether or not this type is for a value that is null.
   *
   * @return True if representing the type of a null value, otherwise false
   */
  override def isNullType: Boolean = _type == null

  protected def newTypeProfile(_type: Type): TypeInfo =
    infoProducer.newTypeInfo(scalaVirtualMachine, _type)
}
