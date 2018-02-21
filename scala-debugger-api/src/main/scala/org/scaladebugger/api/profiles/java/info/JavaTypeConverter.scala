package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import JavaTypeConverter._

object JavaTypeConverter {
  val DefaultNewReferenceTypeProfile: JavaTypeInfo => ReferenceType => ReferenceTypeInfo =
    `type` => `type`.infoProducer.newReferenceTypeInfo(
      `type`.scalaVirtualMachine,
      _: ReferenceType
    )
  val DefaultNewArrayTypeProfile: JavaTypeInfo => ArrayType => ArrayTypeInfo =
    `type` => `type`.infoProducer.newArrayTypeInfo(
      `type`.scalaVirtualMachine,
      _: ArrayType
    )
  val DefaultNewClassTypeProfile: JavaTypeInfo => ClassType => ClassTypeInfo =
    `type` => `type`.infoProducer.newClassTypeInfo(
      `type`.scalaVirtualMachine,
      _: ClassType
    )
  val DefaultNewInterfaceTypeProfile: JavaTypeInfo => InterfaceType => InterfaceTypeInfo =
    `type` => `type`.infoProducer.newInterfaceTypeInfo(
      `type`.scalaVirtualMachine,
      _: InterfaceType
    )
  val DefaultNewPrimitiveTypeProfile: JavaTypeInfo => PrimitiveType => PrimitiveTypeInfo =
    `type` => `type`.infoProducer.newPrimitiveTypeInfo(
      `type`.scalaVirtualMachine,
      _: PrimitiveType
    )
  val DefaultNewVoidTypeProfile: JavaTypeInfo => VoidType => PrimitiveTypeInfo =
    `type` => `type`.infoProducer.newPrimitiveTypeInfo(
      `type`.scalaVirtualMachine,
      _: VoidType
    )

  /**
   * Creates a new converter from the provided type.
   * @param `type` The type to convert
   * @return The converter capable of converting the provided type
   */
  def from(`type`: JavaTypeInfo): JavaTypeConverter =
    new JavaTypeConverter(`type`)()
}

class JavaTypeConverter private(private val `type`: JavaTypeInfo)(
  private val newReferenceTypeProfile: ReferenceType => ReferenceTypeInfo =
    DefaultNewReferenceTypeProfile(`type`),
  private val newArrayTypeProfile: ArrayType => ArrayTypeInfo =
    DefaultNewArrayTypeProfile(`type`),
  private val newClassTypeProfile: ClassType => ClassTypeInfo =
    DefaultNewClassTypeProfile(`type`),
  private val newInterfaceTypeProfile: InterfaceType => InterfaceTypeInfo =
    DefaultNewInterfaceTypeProfile(`type`),
  private val newPrimitiveTypeProfile: PrimitiveType => PrimitiveTypeInfo =
    DefaultNewPrimitiveTypeProfile(`type`),
  private val newVoidTypeProfile: VoidType => PrimitiveTypeInfo =
    DefaultNewVoidTypeProfile(`type`)
) extends TypeConverter {
  /**
   * Creates a copy of the converter that will use the specified converter
   * functions over the current instances.
   * @param newReferenceTypeProfile
   * @param newArrayTypeProfile
   * @param newClassTypeProfile
   * @param newInterfaceTypeProfile
   * @param newPrimitiveTypeProfile
   * @param newVoidTypeProfile
   * @return The new converter instance
   */
  def using(
    newReferenceTypeProfile: ReferenceType => ReferenceTypeInfo = newReferenceTypeProfile,
    newArrayTypeProfile: ArrayType => ArrayTypeInfo = newArrayTypeProfile,
    newClassTypeProfile: ClassType => ClassTypeInfo = newClassTypeProfile,
    newInterfaceTypeProfile: InterfaceType => InterfaceTypeInfo = newInterfaceTypeProfile,
    newPrimitiveTypeProfile: PrimitiveType => PrimitiveTypeInfo = newPrimitiveTypeProfile,
    newVoidTypeProfile: VoidType => PrimitiveTypeInfo = newVoidTypeProfile
  ): JavaTypeConverter = new JavaTypeConverter(`type`)(
    newReferenceTypeProfile = newReferenceTypeProfile,
    newArrayTypeProfile = newArrayTypeProfile,
    newClassTypeProfile = newClassTypeProfile,
    newInterfaceTypeProfile = newInterfaceTypeProfile,
    newPrimitiveTypeProfile = newPrimitiveTypeProfile,
    newVoidTypeProfile = newVoidTypeProfile
  )

  /**
   * Returns the type as an array type (profile).
   *
   * @return The array type profile wrapping this type
   */
  @throws[AssertionError]
  override def toArrayType: ArrayTypeInfo = {
    assert(`type`.isArrayType, "Type must be an array type!")
    newArrayTypeProfile(`type`._type.asInstanceOf[ArrayType])
  }

  /**
   * Returns the type as an class type (profile).
   *
   * @return The class type profile wrapping this type
   */
  @throws[AssertionError]
  override def toClassType: ClassTypeInfo = {
    assert(`type`.isClassType, "Type must be a class type!")
    newClassTypeProfile(`type`._type.asInstanceOf[ClassType])
  }

  /**
   * Returns the type as an interface type (profile).
   *
   * @return The interface type profile wrapping this type
   */
  @throws[AssertionError]
  override def toInterfaceType: InterfaceTypeInfo = {
    assert(`type`.isInterfaceType, "Type must be an interface type!")
    newInterfaceTypeProfile(`type`._type.asInstanceOf[InterfaceType])
  }

  /**
   * Returns the type as an reference type (profile).
   *
   * @return The reference type profile wrapping this type
   */
  @throws[AssertionError]
  override def toReferenceType: ReferenceTypeInfo = {
    assert(`type`.isReferenceType, "Type must be a reference type!")
    newReferenceTypeProfile(`type`._type.asInstanceOf[ReferenceType])
  }

  /**
   * Returns the type as an primitive type (profile).
   *
   * @return The primitive type profile wrapping this type
   */
  @throws[AssertionError]
  override def toPrimitiveType: PrimitiveTypeInfo = {
    assert(`type`.isPrimitiveType, "Type must be a primitive type!")
    `type`._type match {
      case p: PrimitiveType => newPrimitiveTypeProfile(p)
      case v: VoidType      => newPrimitiveTypeProfile(v)
    }
  }
}
