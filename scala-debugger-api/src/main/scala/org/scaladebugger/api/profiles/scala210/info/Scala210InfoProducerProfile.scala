package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureInfoProducerProfile
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce Scala 2.10 info profile instances.
 */
class Scala210InfoProducerProfile extends PureInfoProducerProfile {
  override def newFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: VirtualMachine
  ): FieldVariableInfoProfile = new Scala210FieldInfoProfile(
    scalaVirtualMachine,
    this,
    container,
    field,
    offsetIndex
  )(virtualMachine)

  override def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new Scala210ReferenceTypeInfoProfile(
    scalaVirtualMachine,
    this,
    referenceType
  )

  override def newObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: VirtualMachine,
    referenceType: ReferenceType
  ): ObjectInfoProfile = new Scala210ObjectInfoProfile(
    scalaVirtualMachine,
    this,
    objectReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )
}
