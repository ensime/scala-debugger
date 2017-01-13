package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.{ObjectReference, ReferenceType, VirtualMachine}
import org.scaladebugger.api.profiles.pure.info.PureObjectInfo
import org.scaladebugger.api.profiles.traits.info.{FieldVariableInfo, InfoProducer}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a Scala 2.10 implementation of an object profile.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            object
 * @param infoProducer The producer of info-based profile instances
 * @param _objectReference The reference to the underlying JDI object
 * @param _virtualMachine The virtual machine associated with the object
 * @param _referenceType The reference type for this object
 */
class Scala210ObjectInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _objectReference: ObjectReference
)(
  override protected val _virtualMachine: VirtualMachine = _objectReference.virtualMachine(),
  private val _referenceType: ReferenceType = _objectReference.referenceType()
) extends PureObjectInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _objectReference = _objectReference
)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with Scala210FieldTransformationRules {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = false

  /**
   * Returns all visible fields contained in this object.
   *
   * @note Provides no offset index information!
   * @return The profiles wrapping the visible fields in this object
   */
  override def fields: Seq[FieldVariableInfo] = super.fields.flatMap(transformField)

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  override def fieldOption(
    name: String
  ): Option[FieldVariableInfo] = fields.find(_.name == name)

  /**
    * Returns all visible fields contained in this object with offset index.
    *
    * @return The profiles wrapping the visible fields in this object
    */
  override def indexedFields: Seq[FieldVariableInfo] = fields.zipWithIndex.map {
    case (f, i) => newFieldProfile(f.toJdiInstance, i)
  }

  /**
    * Returns the object's field with the specified name with offset index
    * information.
    *
    * @param name The name of the field
    * @return Some profile wrapping the field, or None if doesn't exist
    */
  override def indexedFieldOption(
    name: String
  ): Option[FieldVariableInfo] = indexedFields.find(_.name == name)
}
