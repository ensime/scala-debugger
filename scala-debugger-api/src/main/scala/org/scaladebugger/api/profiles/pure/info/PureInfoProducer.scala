package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.events.PureEventInfoProducer
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce pure info profile instances.
 */
class PureInfoProducer extends InfoProducer {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: InfoProducer = new PureInfoProducer

  /**
   * Retrieves the event info producer tied to this info producer.
   *
   * @return The information profile for the event producer
   */
  override lazy val eventProducer: EventInfoProducer = {
    new PureEventInfoProducer(this)
  }

  override def newValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo = new PureValueInfo(
    scalaVirtualMachine,
    this,
    value
  )

  override def newArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ArrayInfo = new PureArrayInfo(
    scalaVirtualMachine, this, arrayReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )


  override def newObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ObjectInfo = new PureObjectInfo(
    scalaVirtualMachine,
    this,
    objectReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )

  override def newTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo = new PureTypeInfo(scalaVirtualMachine, this, _type)

  override def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = new PureReferenceTypeInfo(
    scalaVirtualMachine,
    this,
    referenceType
  )

  override def newLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine
  ): IndexedVariableInfo = new PureLocalVariableInfo(
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
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ThreadInfo = new PureThreadInfo(
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
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ThreadGroupInfo = new PureThreadGroupInfo(
    scalaVirtualMachine,
    this,
    threadGroupReference
  )(
    _virtualMachine = virtualMachine,
    _referenceType = referenceType
  )

  override def newClassObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ClassObjectInfo = new PureClassObjectInfo(
    scalaVirtualMachine,
    this,
    classObjectReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine
  )

  override def newClassLoaderInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): ClassLoaderInfo = new PureClassLoaderInfo(
    scalaVirtualMachine,
    this,
    classLoaderReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine
  )

  override def newLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo = new PureLocationInfo(
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
    virtualMachine: => VirtualMachine
  ): FieldVariableInfo = new PureFieldInfo(
    scalaVirtualMachine,
    this,
    container,
    field,
    offsetIndex
  )(virtualMachine)

  override def newMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo = new PureMethodInfo(
    scalaVirtualMachine,
    this,
    method
  )

  override def newFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo = new PureFrameInfo(
    scalaVirtualMachine,
    this,
    stackFrame,
    offsetIndex
  )

  override def newThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfo = new PureThreadStatusInfo(threadReference)

  override def newArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo = new PureArrayTypeInfo(
    scalaVirtualMachine,
    this,
    arrayType
  )

  override def newClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo = new PureClassTypeInfo(
    scalaVirtualMachine,
    this,
    classType
  )

  override def newInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo = new PureInterfaceTypeInfo(
    scalaVirtualMachine,
    this,
    interfaceType
  )

  override def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo = new PurePrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveType)
  )

  override def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo = new PurePrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Right(voidType)
  )

  override def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo = new PurePrimitiveInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveValue)
  )

  override def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo = new PurePrimitiveInfo(
    scalaVirtualMachine,
    this,
    Right(voidValue)
  )

  override def newStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  )(
    virtualMachine: => VirtualMachine,
    referenceType: => ReferenceType
  ): StringInfo = new PureStringInfo(
    scalaVirtualMachine,
    this,
    stringReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine
  )

  override def newTypeCheckerProfile(): TypeChecker =
    new PureTypeChecker
}
