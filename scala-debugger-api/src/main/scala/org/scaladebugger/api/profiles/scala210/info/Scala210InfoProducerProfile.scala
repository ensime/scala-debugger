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
}
