package org.scaladebugger.api.profiles.traits.info

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
  def newDefaultValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo = newValueInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    value = value
  )

  /** Creates a new instance of the value info profile. */
  def newValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo

  /** Fills in additional properties with default values. */
  def newDefaultArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  ): ArrayInfo = newArrayInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    arrayReference = arrayReference
  )()

  /** Creates a new instance of the array info profile. */
  def newArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = arrayReference.referenceType()
  ): ArrayInfo

  /** Fills in additional properties with default values. */
  def newDefaultObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  ): ObjectInfo = newObjectInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    objectReference = objectReference
  )()

  /** Creates a new instance of the object info profile. */
  def newObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = objectReference.referenceType()
  ): ObjectInfo

  /** Fills in additional properties with default values. */
  def newDefaultTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo = newTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    _type = _type
  )

  /** Creates a new instance of the type info profile. */
  def newTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = newReferenceTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    referenceType = referenceType
  )

  /** Creates a new instance of the reference type info profile. */
  def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfo = newLocalVariableInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    frame = frame,
    localVariable = localVariable,
    offsetIndex = offsetIndex
  )()

  /** Creates a new instance of the local variable info profile. */
  def newLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfo,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): IndexedVariableInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  ): ThreadInfo = newThreadInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadReference = threadReference
  )()

  /** Creates a new instance of the thread info profile. */
  def newThreadInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = threadReference.referenceType()
  ): ThreadInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadGroupInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = newThreadGroupInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadGroupReference = threadGroupReference
  )()

  /** Creates a new instance of the thread group info profile. */
  def newThreadGroupInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = threadGroupReference.referenceType()
  ): ThreadGroupInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo = newLocationInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    location = location
  )

  /** Creates a new instance of the location info profile. */
  def newLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfo

  /** Fills in additional properties with default values. */
  def newDefaultFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  ): FieldVariableInfo = newFieldInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    container = container,
    field = field,
    offsetIndex = offsetIndex
  )()

  /** Creates a new instance of the field variable info profile. */
  def newFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): FieldVariableInfo

  /** Fills in additional properties with default values. */
  def newDefaultMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo = newMethodInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    method = method
  )

  /** Creates a new instance of the method info profile. */
  def newMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo

  /** Fills in additional properties with default values. */
  def newDefaultFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo = newFrameInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    stackFrame = stackFrame,
    offsetIndex = offsetIndex
  )

  /** Creates a new instance of the frame info profile. */
  def newFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  ): ClassObjectInfo = newClassObjectInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classObjectReference = classObjectReference
  )()

  /** Creates a new instance of the class object info profile. */
  def newClassObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = classObjectReference.referenceType()
  ): ClassObjectInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassLoaderInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfo = newClassLoaderInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classLoaderReference = classLoaderReference
  )()

  /** Creates a new instance of the class loader info profile. */
  def newClassLoaderInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = classLoaderReference.referenceType()
  ): ClassLoaderInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfo = newThreadStatusInfoProfile(
    threadReference = threadReference
  )

  /** Creates a new instance of the thread status info profile. */
  def newThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfo

  /** Fills in additional properties with default values. */
  def newDefaultArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo = newArrayTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    arrayType = arrayType
  )

  /** Creates a new instance of the array type info profile. */
  def newArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo = newClassTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classType = classType
  )

  /** Creates a new instance of the class type info profile. */
  def newClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo = newInterfaceTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    interfaceType = interfaceType
  )

  /** Creates a new instance of the interface type info profile. */
  def newInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo = newPrimitiveTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveType = primitiveType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo = newPrimitiveTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    voidType = voidType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo = newPrimitiveInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveValue = primitiveValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo = newPrimitiveInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    voidValue = voidValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo

  /** Fills in additional properties with default values. */
  def newDefaultStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  ): StringInfo = newStringInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    stringReference = stringReference
  )()

  /** Creates a new instance of the string info profile. */
  def newStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine,
    referenceType: => ReferenceType = stringReference.referenceType()
  ): StringInfo

  /** Fills in additional properties with default values. */
  def newDefaultTypeCheckerProfile(): TypeChecker =
    newTypeCheckerProfile()

  /** Creates a new instance of the type checker profile. */
  def newTypeCheckerProfile(): TypeChecker
}
