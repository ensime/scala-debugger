package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the generic interface used to produce info instances.
 */
trait InfoProducerProfile extends JavaInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: InfoProducerProfile

  /**
   * Retrieves the event info producer tied to this info producer.
   *
   * @return The information profile for the event producer
   */
  def eventProducer: EventInfoProducerProfile

  /** Fills in additional properties with default values. */
  def newDefaultValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfoProfile = newValueInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    value = value
  )

  /** Creates a new instance of the value info profile. */
  def newValueInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultArrayInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayReference: ArrayReference
  ): ArrayInfoProfile = newArrayInfoProfile(
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
  ): ArrayInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    objectReference: ObjectReference
  ): ObjectInfoProfile = newObjectInfoProfile(
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
  ): ObjectInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfoProfile = newTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    _type = _type
  )

  /** Creates a new instance of the type info profile. */
  def newTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    _type: Type
  ): TypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = newReferenceTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    referenceType = referenceType
  )

  /** Creates a new instance of the reference type info profile. */
  def newReferenceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfoProfile,
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfoProfile = newLocalVariableInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    frame = frame,
    localVariable = localVariable,
    offsetIndex = offsetIndex
  )()

  /** Creates a new instance of the local variable info profile. */
  def newLocalVariableInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    frame: FrameInfoProfile,
    localVariable: LocalVariable,
    offsetIndex: Int
  )(
    virtualMachine: => VirtualMachine = scalaVirtualMachine.underlyingVirtualMachine
  ): IndexedVariableInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultThreadInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadReference: ThreadReference
  ): ThreadInfoProfile = newThreadInfoProfile(
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
  ): ThreadInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultThreadGroupInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfoProfile = newThreadGroupInfoProfile(
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
  ): ThreadGroupInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfoProfile = newLocationInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    location = location
  )

  /** Creates a new instance of the location info profile. */
  def newLocationInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    location: Location
  ): LocationInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultFieldInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    container: Either[ObjectReference, ReferenceType],
    field: Field,
    offsetIndex: Int
  ): FieldVariableInfoProfile = newFieldInfoProfile(
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
  ): FieldVariableInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfoProfile = newMethodInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    method = method
  )

  /** Creates a new instance of the method info profile. */
  def newMethodInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfoProfile = newFrameInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    stackFrame = stackFrame,
    offsetIndex = offsetIndex
  )

  /** Creates a new instance of the frame info profile. */
  def newFrameInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultClassObjectInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classObjectReference: ClassObjectReference
  ): ClassObjectInfoProfile = newClassObjectInfoProfile(
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
  ): ClassObjectInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultClassLoaderInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfoProfile = newClassLoaderInfoProfile(
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
  ): ClassLoaderInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfoProfile = newThreadStatusInfoProfile(
    threadReference = threadReference
  )

  /** Creates a new instance of the thread status info profile. */
  def newThreadStatusInfoProfile(
    threadReference: ThreadReference
  ): ThreadStatusInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfoProfile = newArrayTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    arrayType = arrayType
  )

  /** Creates a new instance of the array type info profile. */
  def newArrayTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfoProfile = newClassTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classType = classType
  )

  /** Creates a new instance of the class type info profile. */
  def newClassTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfoProfile = newInterfaceTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    interfaceType = interfaceType
  )

  /** Creates a new instance of the interface type info profile. */
  def newInterfaceTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfoProfile = newPrimitiveTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveType = primitiveType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfoProfile = newPrimitiveTypeInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    voidType = voidType
  )

  /** Creates a new instance of the primitive type info profile. */
  def newPrimitiveTypeInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfoProfile = newPrimitiveInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    primitiveValue = primitiveValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfoProfile = newPrimitiveInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    voidValue = voidValue
  )

  /** Creates a new instance of the primitive info profile. */
  def newPrimitiveInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultStringInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stringReference: StringReference
  ): StringInfoProfile = newStringInfoProfile(
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
  ): StringInfoProfile

  /** Fills in additional properties with default values. */
  def newDefaultTypeCheckerProfile(): TypeCheckerProfile =
    newTypeCheckerProfile()

  /** Creates a new instance of the type checker profile. */
  def newTypeCheckerProfile(): TypeCheckerProfile
}
