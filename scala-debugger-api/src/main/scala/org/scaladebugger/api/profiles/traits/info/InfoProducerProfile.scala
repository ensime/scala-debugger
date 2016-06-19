package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the generic interface used to produce info instances.
 */
trait InfoProducerProfile {
  def newValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfoProfile

  def newArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = arrayReference.owningThread(),
    referenceType: ReferenceType = arrayReference.referenceType()
  ): ArrayInfoProfile

  def newObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = objectReference.owningThread(),
    referenceType: ReferenceType = objectReference.referenceType()
  ): ObjectInfoProfile

  def newTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfoProfile

  def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile

  def newLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfoProfile,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): IndexedVariableInfoProfile

  def newThreadInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: ReferenceType = threadReference.referenceType()
  ): ThreadInfoProfile

  def newThreadGroupInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = threadGroupReference.owningThread(),
    referenceType: ReferenceType = threadGroupReference.referenceType()
  ): ThreadGroupInfoProfile

  def newLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfoProfile

  def newFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): FieldVariableInfoProfile

  def newMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfoProfile

  def newFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfoProfile

  def newClassObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = classObjectReference.owningThread(),
    referenceType: ReferenceType = classObjectReference.referenceType()
  ): ClassObjectInfoProfile

  def newClassLoaderInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = classLoaderReference.owningThread(),
    referenceType: ReferenceType = classLoaderReference.referenceType()
  ): ClassLoaderInfoProfile

  def newThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfoProfile

  def newArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfoProfile

  def newClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfoProfile

  def newInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfoProfile

  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfoProfile

  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfoProfile

  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfoProfile

  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfoProfile

  def newStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    threadReference: ThreadReference = stringReference.owningThread(),
    referenceType: ReferenceType = stringReference.referenceType()
  ): StringInfoProfile

  def newTypeCheckerProfile(): TypeCheckerProfile
}
