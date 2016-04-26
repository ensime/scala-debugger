package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.{Success, Try}

/**
 * Represents a pure profile for grabbing various information from threads
 * and other objects that adds no extra logic on top of the standard JDI.
 */
trait PureGrabInfoProfile extends GrabInfoProfile {
  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val _virtualMachine: VirtualMachine
  protected val classManager: ClassManager

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param threadReference The thread to associate with the object
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return The new object info profile
   */
  override def `object`(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile = newObjectProfile(threadReference, objectReference)

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return The collection of thread info profiles
   */
  override def threads: Seq[ThreadInfoProfile] = {
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
  ): ThreadInfoProfile = newThreadProfile(threadReference)

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Some profile of the matching thread, or None
   */
  override def threadOption(threadId: Long): Option[ThreadInfoProfile] = {
    threads.find(_.uniqueId == threadId)
  }

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  override def classes: Seq[ReferenceTypeInfoProfile] = {
    classManager.allClasses.map(newReferenceTypeProfile)
  }

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @return Some reference type info profile for the class if found,
   *         otherwise None
   */
  override def classOption(name: String): Option[ReferenceTypeInfoProfile] = {
    classes.find(_.name == name)
  }

  /**
   * Retrieves a reference type profile for the given JDI reference type.
   *
   * @return The reference type info profile wrapping the JDI instance
   */
  override def `class`(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = newReferenceTypeProfile(referenceType)

  /**
   * Retrieves a location profile for the given JDI location.
   *
   * @param location The JDI location with which to wrap in a location
   *                 info profile
   * @return The new location info profile
   */
  override def location(location: Location): LocationInfoProfile =
    newLocationProfile(location)

  /**
   * Retrieves a type info profile for the given JDI type info.
   *
   * @param _type The JDI type with which to wrap in a type info profile
   * @return The new type info profile
   */
  override def `type`(_type: Type): TypeInfoProfile = newTypeProfile(_type)

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
  ): VariableInfoProfile = newFieldProfile(referenceType, field)

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
  ): VariableInfoProfile = newFieldProfile(objectReference, field)

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
  ): VariableInfoProfile = newLocalVariableProfile(stackFrame, localVariable)

  /**
   * Retrieves a stack frame profile for the given JDI stack frame.
   *
   * @param stackFrame The JDI stack frame with which to wrap in a
   *                   frame info profile
   * @return The new frame info profile
   */
  override def stackFrame(stackFrame: StackFrame): FrameInfoProfile =
    newFrameProfile(stackFrame)

  /**
   * Retrieves a method profile for the given JDI method.
   *
   * @param method The JDI method with which to wrap in a method info profile
   * @return The new method info profile
   */
  override def method(method: Method): MethodInfoProfile =
    newMethodProfile(method)

  /**
   * Retrieves a value info profile for the given JDI value info.
   *
   * @param value The JDI value with which to wrap in a value info profile
   * @return The new value info profile
   */
  override def value(value: Value): ValueInfoProfile = newValueProfile(value)

  protected def newThreadProfile(
    threadReference: ThreadReference
  ): ThreadInfoProfile = new PureThreadInfoProfile(
    scalaVirtualMachine,
    threadReference
  )(_virtualMachine = _virtualMachine)

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    new PureTypeInfoProfile(scalaVirtualMachine, _type)

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)

  protected def newLocationProfile(location: Location): LocationInfoProfile =
    new PureLocationInfoProfile(scalaVirtualMachine, location)

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(scalaVirtualMachine, method)

  protected def newFrameProfile(stackFrame: StackFrame): FrameInfoProfile =
    new PureFrameInfoProfile(scalaVirtualMachine, stackFrame, -1)

  protected def newFieldProfile(
    objectReference: ObjectReference,
    field: Field
  ): VariableInfoProfile = new PureFieldInfoProfile(
    scalaVirtualMachine,
    Left(objectReference),
    field,
    -1
  )(_virtualMachine)

  protected def newFieldProfile(
    referenceType: ReferenceType,
    field: Field
  ): VariableInfoProfile = new PureFieldInfoProfile(
    scalaVirtualMachine,
    Right(referenceType),
    field,
    -1
  )(_virtualMachine)

  protected def newLocalVariableProfile(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfoProfile = new PureLocalVariableInfoProfile(
    scalaVirtualMachine,
    newFrameProfile(stackFrame),
    localVariable,
    -1
  )(_virtualMachine)

  protected def newObjectProfile(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile = new PureObjectInfoProfile(
    scalaVirtualMachine,
    objectReference
  )(
    _threadReference = threadReference,
    _virtualMachine = _virtualMachine
  )
}
