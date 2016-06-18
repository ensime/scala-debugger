package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayTypeInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a array type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array type
 * @param infoProducer The producer of info-based profile instances
 * @param _arrayType The underlying JDI array type to wrap
 */
class PureArrayTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val _arrayType: ArrayType
) extends PureReferenceTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _referenceType = _arrayType
) with ArrayTypeInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayType = _arrayType

  /**
   * Returns the JNI signature of the common element in the array.
   *
   * @return The declared type of the elements (the runtime type may be a
   *         subclass of this type)
   */
  override def elementSignature: String = _arrayType.componentSignature()

  /**
   * Returns the name of the type representing the common element in the array.
   *
   * @return The type name as a string
   */
  override def elementTypeName: String = _arrayType.componentTypeName()

  /**
   * Returns the type information for the common element in the array.
   *
   * @return The profile containing type information
   */
  override def elementTypeInfo: TypeInfoProfile =
    newTypeProfile(_arrayType.componentType())

  /**
   * Creates a new instance of the array with the given length.
   *
   * @param length The total length of the array
   * @return The profile representing the new instance
   */
  override def newInstance(length: Int): ArrayInfoProfile =
    newArrayProfile(_arrayType.newInstance(length))

  protected def newArrayProfile(
    arrayReference: ArrayReference
  ): ArrayInfoProfile = infoProducer.newArrayInfoProfile(
    scalaVirtualMachine,
    arrayReference
  )()
}
