package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce pure info profile instances.
 */
class PureInfoProducerProfile extends InfoProducerProfile {
  override def newValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfoProfile = new PureValueInfoProfile(
    scalaVirtualMachine,
    this,
    value
  )

  override def newArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): ArrayInfoProfile = new PureArrayInfoProfile(
    scalaVirtualMachine, this, arrayReference
  )(
    _virtualMachine = virtualMachine,
    _threadReference = threadReference,
    _referenceType = referenceType
  )


  override def newObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): ObjectInfoProfile = new PureObjectInfoProfile(
    scalaVirtualMachine,
    this,
    objectReference
  )(
    _virtualMachine = virtualMachine,
    _threadReference = threadReference,
    _referenceType = referenceType
  )

  override def newTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfoProfile = new PureTypeInfoProfile(scalaVirtualMachine, this, _type)

  override def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine,
    this,
    referenceType
  )

  override def newLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfoProfile,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): IndexedVariableInfoProfile = new PureLocalVariableInfoProfile(
    scalaVirtualMachine,
    this,
    frame,
    localVariable,
    offsetIndex
  )(virtualMachine)

  override def newThreadInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  )(
    virtualMachine: VirtualMachine,
    referenceType: ReferenceType
  ): ThreadInfoProfile = new PureThreadInfoProfile(
    scalaVirtualMachine,
    this,
    threadReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )

  override def newThreadGroupInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): ThreadGroupInfoProfile = new PureThreadGroupInfoProfile(
    scalaVirtualMachine,
    this,
    threadGroupReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType,
    _threadReference = threadReference
  )

  override def newClassObjectProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): ClassObjectInfoProfile = new PureClassObjectInfoProfile(
    scalaVirtualMachine,
    this,
    classObjectReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine,
    _threadReference = threadReference
  )

  override def newClassLoaderProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): ClassLoaderInfoProfile = new PureClassLoaderInfoProfile(
    scalaVirtualMachine,
    this,
    classLoaderReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine,
    _threadReference = threadReference
  )

  override def newLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfoProfile = new PureLocationInfoProfile(
    scalaVirtualMachine,
    this,
    location
  )

  override def newFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: VirtualMachine
  ): FieldVariableInfoProfile = new PureFieldInfoProfile(
    scalaVirtualMachine,
    this,
    container,
    field,
    offsetIndex
  )(virtualMachine)

  override def newMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfoProfile = new PureMethodInfoProfile(
    scalaVirtualMachine,
    this,
    method
  )

  override def newFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfoProfile = new PureFrameInfoProfile(
    scalaVirtualMachine,
    this,
    stackFrame,
    offsetIndex
  )

  override def newThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfoProfile = new PureThreadStatusInfoProfile(threadReference)

  override def newArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfoProfile = new PureArrayTypeInfoProfile(
    scalaVirtualMachine,
    this,
    arrayType
  )

  override def newClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfoProfile = new PureClassTypeInfoProfile(
    scalaVirtualMachine,
    this,
    classType
  )

  override def newInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfoProfile = new PureInterfaceTypeInfoProfile(
    scalaVirtualMachine,
    this,
    interfaceType
  )

  override def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfoProfile = new PurePrimitiveTypeInfoProfile(
    scalaVirtualMachine,
    this,
    Left(primitiveType)
  )

  override def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfoProfile = new PurePrimitiveTypeInfoProfile(
    scalaVirtualMachine,
    this,
    Right(voidType)
  )

  override def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfoProfile = new PurePrimitiveInfoProfile(
    scalaVirtualMachine,
    this,
    Left(primitiveValue)
  )

  override def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfoProfile = new PurePrimitiveInfoProfile(
    scalaVirtualMachine,
    this,
    Right(voidValue)
  )

  override def newStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  )(
    virtualMachine: VirtualMachine,
    threadReference: ThreadReference,
    referenceType: ReferenceType
  ): StringInfoProfile = new PureStringInfoProfile(
    scalaVirtualMachine,
    this,
    stringReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine,
    _threadReference = threadReference
  )

  override def newTypeCheckerProfile(): TypeCheckerProfile =
    new PureTypeCheckerProfile
}
