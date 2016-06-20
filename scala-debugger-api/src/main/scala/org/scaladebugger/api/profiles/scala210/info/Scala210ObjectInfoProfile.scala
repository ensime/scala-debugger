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
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  override def fieldOption(
    name: String
  ): Option[FieldVariableInfoProfile] = fields.find(_.name == name)
}
