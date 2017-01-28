package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.annotation.tailrec
import scala.util.{Success, Try}

/**
 * Represents a java profile for grabbing various information from threads
 * and other objects that adds no extra logic on top of the standard JDI.
 */
trait JavaGrabInfoProfile extends GrabInfoProfile {
  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer
  protected val _virtualMachine: VirtualMachine
  protected val classManager: ClassManager

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return The new object info profile
   */
  override def `object`(objectReference: ObjectReference): ObjectInfo =
    newObjectProfile(objectReference)

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return The collection of thread info profiles
   */
  override def threads: Seq[ThreadInfo] = {
    import scala.collection.JavaConverters._
    _virtualMachine.allThreads().asScala.map(newThreadProfile)
  }

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return The new thread info profile
   */
  override def thread(
    threadReference: ThreadReference
  ): ThreadInfo = newThreadProfile(threadReference)

  /**
   * Retrieves all thread groups contained in the remote JVM.
   *
   * @return The collection of thread group info profiles
   */
  override def threadGroups: Seq[ThreadGroupInfo] = {
    import scala.collection.JavaConverters._
    _virtualMachine.topLevelThreadGroups().asScala.map(newThreadGroupProfile)
  }

  /**
   * Retrieves a threadGroup group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupReference The JDI thread group reference with which to
   *                             wrap in a thread group info profile
   * @return The profile of the matching thread group, or throws an exception
   */
  override def threadGroup(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = newThreadGroupProfile(threadGroupReference)

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  override def classes: Seq[ReferenceTypeInfo] = {
    classManager.allClasses.map(newReferenceTypeProfile)
  }

  /**
   * Retrieves a reference type profile for the given JDI reference type.
   *
   * @return The reference type info profile wrapping the JDI instance
   */
  override def `class`(
    referenceType: ReferenceType
  ): ReferenceTypeInfo = newReferenceTypeProfile(referenceType)

  /**
   * Retrieves a location profile for the given JDI location.
   *
   * @param location The JDI location with which to wrap in a location
   *                 info profile
   * @return The new location info profile
   */
  override def location(location: Location): LocationInfo =
    newLocationProfile(location)

  /**
   * Retrieves a type info profile for the given JDI type info.
   *
   * @param _type The JDI type with which to wrap in a type info profile
   * @return The new type info profile
   */
  override def `type`(_type: Type): TypeInfo = newTypeProfile(_type)

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceType The reference type to associate with the field
   * @param field         The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  override def field(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfo = newFieldProfile(referenceType, field)

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectReference The object reference to associate with the field
   * @param field           The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  override def field(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfo = newFieldProfile(objectReference, field)

  /**
   * Retrieves a localVariable profile for the given JDI local variable.
   *
   * @param stackFrame    The stack frame to associate with the
   *                      local variable
   * @param localVariable The JDI local variable with which to wrap in a
   *                      variable info profile
   * @return The variable profile representing the local variable
   */
  override def localVariable(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfo = newLocalVariableProfile(stackFrame, localVariable)

  /**
   * Retrieves a stack frame profile for the given JDI stack frame.
   *
   * @param stackFrame The JDI stack frame with which to wrap in a
   *                   frame info profile
   * @return The new frame info profile
   */
  override def stackFrame(stackFrame: StackFrame): FrameInfo =
    newFrameProfile(stackFrame)

  /**
   * Retrieves a method profile for the given JDI method.
   *
   * @param method The JDI method with which to wrap in a method info profile
   * @return The new method info profile
   */
  override def method(method: Method): MethodInfo =
    newMethodProfile(method)

  /**
   * Retrieves a value info profile for the given JDI value info.
   *
   * @param value The JDI value with which to wrap in a value info profile
   * @return The new value info profile
   */
  override def value(value: Value): ValueInfo = newValueProfile(value)

  protected def newThreadProfile(
    threadReference: ThreadReference
  ): ThreadInfo = infoProducer.newThreadInfo(
    scalaVirtualMachine,
    threadReference
  )(virtualMachine = _virtualMachine)

  protected def newThreadGroupProfile(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = infoProducer.newThreadGroupInfo(
    scalaVirtualMachine,
    threadGroupReference
  )(virtualMachine = _virtualMachine)

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfo = infoProducer.newReferenceTypeInfo(
    scalaVirtualMachine,
    referenceType
  )

  protected def newTypeProfile(_type: Type): TypeInfo =
    infoProducer.newTypeInfo(scalaVirtualMachine, _type)

  protected def newValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfo(scalaVirtualMachine, value)

  protected def newLocationProfile(location: Location): LocationInfo =
    infoProducer.newLocationInfo(scalaVirtualMachine, location)

  protected def newMethodProfile(method: Method): MethodInfo =
    infoProducer.newMethodInfo(scalaVirtualMachine, method)

  protected def newFrameProfile(stackFrame: StackFrame): FrameInfo =
    infoProducer.newFrameInfo(scalaVirtualMachine, stackFrame, -1)

  protected def newFieldProfile(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfo = infoProducer.newFieldInfo(
    scalaVirtualMachine,
    Left(objectReference),
    field,
    -1
  )(_virtualMachine)

  protected def newFieldProfile(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfo = infoProducer.newFieldInfo(
    scalaVirtualMachine,
    Right(referenceType),
    field,
    -1
  )(_virtualMachine)

  protected def newLocalVariableProfile(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfo = infoProducer.newLocalVariableInfo(
    scalaVirtualMachine,
    newFrameProfile(stackFrame),
    localVariable,
    -1
  )(virtualMachine = _virtualMachine)

  protected def newObjectProfile(
    objectReference: ObjectReference
  ): ObjectInfo = infoProducer.newObjectInfo(
    scalaVirtualMachine,
    objectReference
  )(
    virtualMachine = _virtualMachine
  )
}
