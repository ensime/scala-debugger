package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayTypeInfoProfile, ReferenceTypeInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import PureTypeInfoProfile._

object PureTypeInfoProfile {
  val DefaultNullTypeName = "null"
  val DefaultNullTypeSignature = "null"
}

/**
 * Represents a pure implementation of a type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param infoProducer The producer of info-based profile instances
 * @param _type The reference to the underlying JDI type
 */
class PureTypeInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducerProfile,
  private val _type: Type
) extends TypeInfoProfile {
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
  override def toJavaInfo: TypeInfoProfile = {
    infoProducer.toJavaInfo.newTypeInfoProfile(
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

  /**
   * Returns the type as an array type (profile).
   *
   * @return The array type profile wrapping this type
   */
  @throws[AssertionError]
  override def toArrayType: ArrayTypeInfoProfile = {
    assert(isArrayType, "Type must be an array type!")
    newArrayTypeProfile(_type.asInstanceOf[ArrayType])
  }

  /**
   * Returns the type as an class type (profile).
   *
   * @return The class type profile wrapping this type
   */
  @throws[AssertionError]
  override def toClassType: ClassTypeInfoProfile = {
    assert(isClassType, "Type must be a class type!")
    newClassTypeProfile(_type.asInstanceOf[ClassType])
  }

  /**
   * Returns the type as an interface type (profile).
   *
   * @return The interface type profile wrapping this type
   */
  @throws[AssertionError]
  override def toInterfaceType: InterfaceTypeInfoProfile = {
    assert(isInterfaceType, "Type must be an interface type!")
    newInterfaceTypeProfile(_type.asInstanceOf[InterfaceType])
  }

  /**
   * Returns the type as an reference type (profile).
   *
   * @return The reference type profile wrapping this type
   */
  @throws[AssertionError]
  override def toReferenceType: ReferenceTypeInfoProfile = {
    assert(isReferenceType, "Type must be a reference type!")
    newReferenceTypeProfile(_type.asInstanceOf[ReferenceType])
  }

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return The primitive type profile wrapping this type
   */
  @throws[AssertionError]
  override def toPrimitiveType: PrimitiveTypeInfoProfile = {
    assert(isPrimitiveType, "Type must be a primitive type!")
    _type match {
      case p: PrimitiveType => newPrimitiveTypeProfile(p)
      case v: VoidType      => newPrimitiveTypeProfile(v)
    }
  }

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    infoProducer.newTypeInfoProfile(scalaVirtualMachine, _type)

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = infoProducer.newReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  protected def newArrayTypeProfile(
    arrayType: ArrayType
  ): ArrayTypeInfoProfile = infoProducer.newArrayTypeInfoProfile(
    scalaVirtualMachine,
    arrayType
  )

  protected def newClassTypeProfile(
    classType: ClassType
  ): ClassTypeInfoProfile = infoProducer.newClassTypeInfoProfile(
    scalaVirtualMachine,
    classType
  )

  protected def newInterfaceTypeProfile(
    interfaceType: InterfaceType
  ): InterfaceTypeInfoProfile = infoProducer.newInterfaceTypeInfoProfile(
    scalaVirtualMachine,
    interfaceType
  )

  protected def newPrimitiveTypeProfile(
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfoProfile = infoProducer.newPrimitiveTypeInfoProfile(
    scalaVirtualMachine,
    primitiveType
  )

  protected def newPrimitiveTypeProfile(
    voidType: VoidType
  ): PrimitiveTypeInfoProfile = infoProducer.newPrimitiveTypeInfoProfile(
    scalaVirtualMachine,
    voidType
  )
}
