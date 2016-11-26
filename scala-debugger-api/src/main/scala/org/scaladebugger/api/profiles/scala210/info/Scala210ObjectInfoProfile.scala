package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.{ObjectReference, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.profiles.pure.info.PureObjectInfoProfile
import org.scaladebugger.api.profiles.traits.info.{FieldVariableInfoProfile, InfoProducerProfile}
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
class Scala210ObjectInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val _objectReference: ObjectReference
)(
  override protected val _virtualMachine: VirtualMachine = _objectReference.virtualMachine(),
  private val _referenceType: ReferenceType = _objectReference.referenceType()
) extends PureObjectInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _objectReference = _objectReference
)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) {
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
  override def fields: Seq[FieldVariableInfoProfile] = {
    val baseFields = super.fields

    // Will retrieve fields of these objects
    val expandNames = Seq("$outer")

    // Will skip these fields
    // TODO: Provide better means of skipping executionStart
    //       since this avoids it even if someone added it directly
    val ignoreNames = Seq("MODULE$", "executionStart", "serialVersionUID")

    // Will skip fields whose names start with these prefixes
    val ignoreNamePrefix = Seq("scala$")

    // Will skip fields whose origin starts with these strings
    val ignoreOrigin = Seq("scala.")

    // TODO: Handle infinitely-recursive fields when expanding
    def transformField(field: FieldVariableInfoProfile): Seq[FieldVariableInfoProfile] = {
      val jField = field.toJavaInfo
      val value = field.toValueInfo

      // If the field is something we should ignore directly, do so
      if (ignoreNames.contains(jField.name)) Nil

      // If the field name starts with a prefix we should ignore, do so
      else if (ignoreNamePrefix.exists(jField.name.startsWith)) Nil

      // If the field's origin starts with an origin we don't want, ignore it
      else if (ignoreOrigin.exists(jField.declaringTypeInfo.name.startsWith)) Nil

      // If the field is one that should be expanded, do so
      else if (value.isObject && expandNames.contains(jField.name))
        value.toObjectInfo.fields.flatMap(transformField)

      // Otherwise, return the normal field
      else Seq(field)
    }

    baseFields.flatMap(transformField)
  }

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  override def fieldOption(
    name: String
  ): Option[FieldVariableInfoProfile] = fields.find(_.name == name)
}
