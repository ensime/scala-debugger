package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.java.info.JavaInfoProducer
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce Scala 2.10 info profile instances.
 */
class Scala210InfoProducer extends JavaInfoProducer {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = false

  override def newFieldInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine
  ): FieldVariableInfo = new Scala210FieldInfo(
    scalaVirtualMachine,
    this,
    container,
    field,
    offsetIndex
  )(virtualMachine)

  override def newLocalVariableInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine
  ): IndexedVariableInfo = new Scala210LocalVariableInfo(
    scalaVirtualMachine,
    this,
    frame,
    localVariable,
    offsetIndex
  )(virtualMachine)

  override def newReferenceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = new Scala210ReferenceTypeInfo(
    scalaVirtualMachine,
    this,
    referenceType
  )

  override def newObjectInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ObjectInfo = new Scala210ObjectInfo(
    scalaVirtualMachine,
    this,
    objectReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )
}
