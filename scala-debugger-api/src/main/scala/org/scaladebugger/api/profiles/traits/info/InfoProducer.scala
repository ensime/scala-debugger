package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the generic interface used to produce info instances.
 */
trait InfoProducer extends JavaInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: InfoProducer

  /**
   * Retrieves the event info producer tied to this info producer.
   *
   * @return The information profile for the event producer
   */
  def eventProducer: EventInfoProducer

  /** Fills in additional properties with default values. */
  def newDefaultValueInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo = newValueInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    value = value
  )

  /** Creates a new instance of the value info profile. */
  def newValueInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo

  /** Fills in additional properties with default values. */
  def newDefaultArrayInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  ): ArrayInfo = newArrayInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    arrayReference = arrayReference
  )()

  /** Creates a new instance of the array info profile. */
  def newArrayInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = arrayReference.referenceType()
  ): ArrayInfo

  /** Fills in additional properties with default values. */
  def newDefaultObjectInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  ): ObjectInfo = newObjectInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    objectReference = objectReference
  )()

  /** Creates a new instance of the object info profile. */
  def newObjectInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = objectReference.referenceType()
  ): ObjectInfo

  /** Fills in additional properties with default values. */
  def newDefaultTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo = newTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    _type = _type
  )

  /** Creates a new instance of the type info profile. */
  def newTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultReferenceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = newReferenceTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    referenceType = referenceType
  )

  /** Creates a new instance of the reference type info profile. */
  def newReferenceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocalVariableInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfo = newLocalVariableInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    frame = frame,
    localVariable = localVariable,
    offsetIndex = offsetIndex
  )()

  /** Creates a new instance of the local variable info profile. */
  def newLocalVariableInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): IndexedVariableInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  ): ThreadInfo = newThreadInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    threadReference = threadReference
  )()

  /** Creates a new instance of the thread info profile. */
  def newThreadInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = threadReference.referenceType()
  ): ThreadInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadGroupInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = newThreadGroupInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    threadGroupReference = threadGroupReference
  )()

  /** Creates a new instance of the thread group info profile. */
  def newThreadGroupInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = threadGroupReference.referenceType()
  ): ThreadGroupInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocationInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo = newLocationInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    location = location
  )

  /** Creates a new instance of the location info profile. */
  def newLocationInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo

  /** Fills in additional properties with default values. */
  def newDefaultFieldInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  ): FieldVariableInfo = newFieldInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    container = container,
    field = field,
    offsetIndex = offsetIndex
  )()

  /** Creates a new instance of the field variable info profile. */
  def newFieldInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): FieldVariableInfo

  /** Fills in additional properties with default values. */
  def newDefaultMethodInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo = newMethodInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    method = method
  )

  /** Creates a new instance of the method info profile. */
  def newMethodInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo

  /** Fills in additional properties with default values. */
  def newDefaultFrameInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo = newFrameInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    stackFrame = stackFrame,
    offsetIndex = offsetIndex
  )

  /** Creates a new instance of the frame info profile. */
  def newFrameInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassObjectInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  ): ClassObjectInfo = newClassObjectInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    classObjectReference = classObjectReference
  )()

  /** Creates a new instance of the class object info profile. */
  def newClassObjectInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = classObjectReference.referenceType()
  ): ClassObjectInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassLoaderInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfo = newClassLoaderInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    classLoaderReference = classLoaderReference
  )()

  /** Creates a new instance of the class loader info profile. */
  def newClassLoaderInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = classLoaderReference.referenceType()
  ): ClassLoaderInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadStatusInfo(
    threadReference: ThreadReference
  ): ThreadStatusInfo = newThreadStatusInfo(
    threadReference = threadReference
  )

  /** Creates a new instance of the thread status info profile. */
  def newThreadStatusInfo(
    threadReference: ThreadReference
  ): ThreadStatusInfo

  /** Fills in additional properties with default values. */
  def newDefaultArrayTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo = newArrayTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    arrayType = arrayType
  )

  /** Creates a new instance of the array type info profile. */
  def newArrayTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo = newClassTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    classType = classType
  )

  /** Creates a new instance of the class type info profile. */
  def newClassTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultInterfaceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo = newInterfaceTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    interfaceType = interfaceType
  )

  /** Creates a new instance of the interface type info profile. */
  def newInterfaceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo = newPrimitiveTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveType = primitiveType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo = newPrimitiveTypeInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    voidType = voidType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo = newPrimitiveInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveValue = primitiveValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo = newPrimitiveInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    voidValue = voidValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo

  /** Fills in additional properties with default values. */
  def newDefaultStringInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  ): StringInfo = newStringInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    stringReference = stringReference
  )()

  /** Creates a new instance of the string info profile. */
  def newStringInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = stringReference.referenceType()
  ): StringInfo

  /** Fills in additional properties with default values. */
  def newDefaultTypeChecker(): TypeChecker = newTypeChecker()

  /** Creates a new instance of the type checker profile. */
  def newTypeChecker(): TypeChecker
}
