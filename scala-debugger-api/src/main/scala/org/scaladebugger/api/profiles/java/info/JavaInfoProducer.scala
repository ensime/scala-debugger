package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.java.info.events.JavaEventInfoProducer
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce java info profile instances.
 */
class JavaInfoProducer extends InfoProducer {
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
  override def toJavaInfo: InfoProducer = this

  /**
   * Retrieves the event info producer tied to this info producer.
   *
   * @return The information profile for the event producer
   */
  override lazy val eventProducer: EventInfoProducer = {
    new JavaEventInfoProducer(this)
  }

  override def newValueInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    value: Value
  ): ValueInfo = new JavaValueInfo(
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
  ): ArrayInfo = new JavaArrayInfo(
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
  ): ObjectInfo = new JavaObjectInfo(
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
  ): TypeInfo = new JavaTypeInfo(scalaVirtualMachine, this, _type)

  override def newReferenceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    referenceType: ReferenceType
  ): ReferenceTypeInfo = new JavaReferenceTypeInfo(
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
  ): IndexedVariableInfo = new JavaLocalVariableInfo(
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
  ): ThreadInfo = new JavaThreadInfo(
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
  ): ThreadGroupInfo = new JavaThreadGroupInfo(
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
  ): ClassObjectInfo = new JavaClassObjectInfo(
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
  ): ClassLoaderInfo = new JavaClassLoaderInfo(
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
  ): LocationInfo = new JavaLocationInfo(
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
  ): FieldVariableInfo = new JavaFieldInfo(
    scalaVirtualMachine,
    this,
    container,
    field,
    offsetIndex
  )(virtualMachine)

  override def newMethodInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    method: Method
  ): MethodInfo = new JavaMethodInfo(
    scalaVirtualMachine,
    this,
    method
  )

  override def newFrameInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stackFrame: StackFrame,
    offsetIndex: Int
  ): FrameInfo = new JavaFrameInfo(
    scalaVirtualMachine,
    this,
    stackFrame,
    offsetIndex
  )

  override def newThreadStatusInfo(
    threadReference: ThreadReference
  ): ThreadStatusInfo = new JavaThreadStatusInfo(threadReference)

  override def newArrayTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    arrayType: ArrayType
  ): ArrayTypeInfo = new JavaArrayTypeInfo(
    scalaVirtualMachine,
    this,
    arrayType
  )

  override def newClassTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classType: ClassType
  ): ClassTypeInfo = new JavaClassTypeInfo(
    scalaVirtualMachine,
    this,
    classType
  )

  override def newInterfaceTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    interfaceType: InterfaceType
  ): InterfaceTypeInfo = new JavaInterfaceTypeInfo(
    scalaVirtualMachine,
    this,
    interfaceType
  )

  override def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveType: PrimitiveType
  ): PrimitiveTypeInfo = new JavaPrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveType)
  )

  override def newPrimitiveTypeInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidType: VoidType
  ): PrimitiveTypeInfo = new JavaPrimitiveTypeInfo(
    scalaVirtualMachine,
    this,
    Right(voidType)
  )

  override def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    primitiveValue: PrimitiveValue
  ): PrimitiveInfo = new JavaPrimitiveInfo(
    scalaVirtualMachine,
    this,
    Left(primitiveValue)
  )

  override def newPrimitiveInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    voidValue: VoidValue
  ): PrimitiveInfo = new JavaPrimitiveInfo(
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
  ): StringInfo = new JavaStringInfo(
    scalaVirtualMachine,
    this,
    stringReference
  )(
    _referenceType = referenceType,
    _virtualMachine = virtualMachine
  )

  override def newTypeChecker(): TypeChecker =
    new JavaTypeChecker
}
