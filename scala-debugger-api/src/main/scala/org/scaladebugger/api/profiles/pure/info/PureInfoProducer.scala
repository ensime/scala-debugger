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

  override def newValueInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo = new PureValueInfo(
    scalaVirtualMachine,
    this,
    value
  )

  override def newArrayInfo(
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


  override def newObjectInfo(
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

  override def newTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo = new PureTypeInfo(scalaVirtualMachine, this, _type)

  override def newReferenceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = new PureReferenceTypeInfo(
    scalaVirtualMachine,
    this,
    referenceType
  )

  override def newLocalVariableInfo(
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

  override def newThreadInfo(
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

  override def newThreadGroupInfo(
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

  override def newClassObjectInfo(
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

  override def newClassLoaderInfo(
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

  override def newLocationInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo = new PureLocationInfo(
    scalaVirtualMachine,
    this,
    location
  )

  override def newFieldInfo(
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

  override def newMethodInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo = new PureMethodInfo(
    scalaVirtualMachine,
    this,
    method
  )

  override def newFrameInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo = new PureFrameInfo(
    scalaVirtualMachine,
    this,
    stackFrame,
    offsetIndex
  )

  override def newThreadStatusInfo(
    threadReference: ThreadReference
  ): ThreadStatusInfo = new PureThreadStatusInfo(threadReference)

  override def newArrayTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo = new PureArrayTypeInfo(
    scalaVirtualMachine,
    this,
    arrayType
  )

  override def newClassTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo = new PureClassTypeInfo(
    scalaVirtualMachine,
    this,
    classType
  )

  override def newInterfaceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo = new PureInterfaceTypeInfo(
    scalaVirtualMachine,
    this,
    interfaceType
  )

  override def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo = new PurePrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveType)
  )

  override def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo = new PurePrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Right(voidType)
  )

  override def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo = new PurePrimitiveInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveValue)
  )

  override def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo = new PurePrimitiveInfo(
    scalaVirtualMachine,
    this,
    Right(voidValue)
  )

  override def newStringInfo(
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

  override def newTypeChecker(): TypeChecker =
    new PureTypeChecker
}
