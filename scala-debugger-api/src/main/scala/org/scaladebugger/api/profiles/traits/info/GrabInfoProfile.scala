package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi._
import java.util.NoSuchElementException

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * the ability to grab various information for a specific debug profile.
 */
trait GrabInfoProfile {
  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param threadInfo The information about the thread to associate with the
   *                   object
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return Success containing the object profile, otherwise a failure
   */
  def tryObject(
    threadInfo: ThreadInfoProfile,
    objectReference: ObjectReference
  ): Try[ObjectInfoProfile] =
    Try(`object`(threadInfo, objectReference))

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param threadInfo The information about the thread to associate with the
   *                   object
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return The new object info profile
   */
  def `object`(
    threadInfo: ThreadInfoProfile,
    objectReference: ObjectReference
  ): ObjectInfoProfile = `object`(threadInfo.toJdiInstance, objectReference)

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param threadReference The thread to associate with the object
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return Success containing the object profile, otherwise a failure
   */
  def tryObject(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): Try[ObjectInfoProfile] =
    Try(`object`(threadReference, objectReference))

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param threadReference The thread to associate with the object
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return The new object info profile
   */
  def `object`(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return Success containing the collection of thread info profiles,
   *         otherwise a failure
   */
  def tryThreads: Try[Seq[ThreadInfoProfile]] = Try(threads)

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return The collection of thread info profiles
   */
  def threads: Seq[ThreadInfoProfile]

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return Success containing the thread profile, otherwise a failure
   */
  def tryThread(threadReference: ThreadReference): Try[ThreadInfoProfile] =
    Try(thread(threadReference))

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return The new thread info profile
   */
  def thread(threadReference: ThreadReference): ThreadInfoProfile

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryThread(threadId: Long): Try[ThreadInfoProfile] =
    Try(thread(threadId))

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return The profile of the matching thread, or throws an exception
   */
  def thread(threadId: Long): ThreadInfoProfile = {
    val t = threadOption(threadId)

    if (t.isEmpty)
      throw new NoSuchElementException(s"No thread with $threadId found!")

    t.get
  }

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Some profile of the matching thread, or None
   */
  def threadOption(threadId: Long): Option[ThreadInfoProfile]

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupReference The JDI thread group reference with which to
   *                             wrap in a thread group info profile
   * @return Success containing the thread group profile if found, otherwise
   *         a failure
   */
  def tryThreadGroup(
    threadGroupReference: ThreadGroupReference
  ): Try[ThreadGroupInfoProfile] = Try(threadGroup(threadGroupReference))

  /**
   * Retrieves a threadGroup group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupReference The JDI thread group reference with which to
   *                             wrap in a thread group info profile
   * @return The profile of the matching thread group, or throws an exception
   */
  def threadGroup(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfoProfile

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupId The id of the thread group
   * @return Success containing the thread group profile if found, otherwise
   *         a failure
   */
  def tryThreadGroup(threadGroupId: Long): Try[ThreadGroupInfoProfile] =
    Try(threadGroup(threadGroupId))

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupId The id of the thread group
   * @return The profile of the matching thread group, or throws an exception
   */
  def threadGroup(threadGroupId: Long): ThreadGroupInfoProfile = {
    val tg = threadGroupOption(threadGroupId)

    if (tg.isEmpty) throw new NoSuchElementException(
      s"No thread group with $threadGroupId found!")

    tg.get
  }

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupId The id of the thread group
   * @return Some profile of the matching thread group, or None
   */
  def threadGroupOption(threadGroupId: Long): Option[ThreadGroupInfoProfile]

  /**
   * Retrieves all thread groups contained in the remote JVM.
   *
   * @return Success containing the collection of thread group info profiles,
   *         otherwise a failure
   */
  def tryThreadGroups: Try[Seq[ThreadGroupInfoProfile]] = Try(threadGroups)

  /**
   * Retrieves all thread groups contained in the remote JVM.
   *
   * @return The collection of thread group info profiles
   */
  def threadGroups: Seq[ThreadGroupInfoProfile]

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return Success containing the collection of reference type info profiles,
   *         otherwise a failure
   */
  def tryClasses: Try[Seq[ReferenceTypeInfoProfile]] = Try(classes)

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  def classes: Seq[ReferenceTypeInfoProfile]

  /**
   * Retrieves a reference type profile for the given JDI reference type.
   *
   * @return The reference type info profile wrapping the JDI instance
   */
  def `class`(referenceType: ReferenceType): ReferenceTypeInfoProfile

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return Success containing the reference type info profile for the class,
   *         otherwise a failure
   */
  def tryClass(name: String): Try[ReferenceTypeInfoProfile] = Try(`class`(name))

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return The reference type info profile for the class
   */
  def `class`(name: String): ReferenceTypeInfoProfile = {
    val c = classOption(name)

    if (c.isEmpty)
      throw new NoSuchElementException(s"Class with name '$name' not found!")

    c.get
  }

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return Some reference type info profile for the class if found,
   *         otherwise None
   */
  def classOption(name: String): Option[ReferenceTypeInfoProfile]

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceType The reference type to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return Success containing the variable profile representing the field,
   *         otherwise a failure
   */
  def tryField(
    referenceType: ReferenceType,
    field: Field
  ): Try[FieldVariableInfoProfile] = Try(this.field(referenceType, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceType The reference type to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfoProfile

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceTypeInfo The information about the reference type to
   *                          associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return Success containing the variable profile representing the field,
   *         otherwise a failure
   */
  def tryField(
    referenceTypeInfo: ReferenceTypeInfoProfile,
    field: Field
  ): Try[FieldVariableInfoProfile] = Try(this.field(referenceTypeInfo, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceTypeInfo The information about the reference type to
   *                          associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    referenceTypeInfo: ReferenceTypeInfoProfile,
    field: Field
  ): FieldVariableInfoProfile =
    this.field(referenceTypeInfo.toJdiInstance, field)

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectReference The object reference to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return Success containing the variable profile representing the field,
   *         otherwise a failure
   */
  def tryField(
    objectReference: ObjectReference,
    field: Field
  ): Try[FieldVariableInfoProfile] = Try(this.field(objectReference, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectReference The object reference to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfoProfile

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectInfo The information about the object to associate with
   *                   the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return Success containing the variable profile representing the field,
   *         otherwise a failure
   */
  def tryField(
    objectInfo: ObjectInfoProfile,
    field: Field
  ): Try[FieldVariableInfoProfile] = Try(this.field(objectInfo, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectInfo The information about the object to associate with
   *                   the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    objectInfo: ObjectInfoProfile,
    field: Field
  ): FieldVariableInfoProfile = this.field(objectInfo.toJdiInstance, field)

  /**
   * Retrieves a local variable profile for the given JDI local variable.
   *
   * @param stackFrame The stack frame to associate with the
   *                      local variable
   * @param localVariable The JDI local variable with which to wrap in a
   *                      variable info profile
   * @return Success containing the variable profile representing the
   *         local variable, otherwise a failure
   */
  def tryLocalVariable(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): Try[VariableInfoProfile] = Try(this.localVariable(
    stackFrame, localVariable
  ))

  /**
   * Retrieves a localVariable profile for the given JDI local variable.
   *
   * @param stackFrame The stack frame to associate with the
   *                      local variable
   * @param localVariable The JDI local variable with which to wrap in a
   *                      variable info profile
   * @return The variable profile representing the local variable
   */
  def localVariable(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfoProfile

  /**
   * Retrieves a localVariable profile for the given JDI local variable.
   *
   * @param stackFrameInfo The information about the stack frame to
   *                          associate with the localVariable
   * @param localVariable The JDI local variable with which to wrap in a
   *                      variable info profile
   * @return Success containing the variable profile representing the
   *         local variable, otherwise a failure
   */
  def tryLocalVariable(
    stackFrameInfo: FrameInfoProfile,
    localVariable: LocalVariable
  ): Try[VariableInfoProfile] = Try(this.localVariable(
    stackFrameInfo, localVariable
  ))

  /**
   * Retrieves a localVariable profile for the given JDI local variable.
   *
   * @param stackFrameInfo The information about the stack frame to
   *                          associate with the local variable
   * @param localVariable The JDI local variable with which to wrap in a
   *                      variable info profile
   * @return The variable profile representing the local variable
   */
  def localVariable(
    stackFrameInfo: FrameInfoProfile,
    localVariable: LocalVariable
  ): VariableInfoProfile = this.localVariable(
    stackFrameInfo.toJdiInstance,
    localVariable
  )

  /**
   * Retrieves a location profile for the given JDI location.
   *
   * @param location The JDI location with which to wrap in a location
   *                 info profile
   * @return The new location info profile
   */
  def location(location: Location): LocationInfoProfile

  /**
   * Retrieves a method profile for the given JDI method.
   *
   * @param method The JDI method with which to wrap in a method info profile
   * @return The new method info profile
   */
  def method(method: Method): MethodInfoProfile

  /**
   * Retrieves a stack frame profile for the given JDI stack frame.
   *
   * @param stackFrame The JDI stack frame with which to wrap in a
   *                   frame info profile
   * @return The new frame info profile
   */
  def stackFrame(stackFrame: StackFrame): FrameInfoProfile

  /**
   * Retrieves a type info profile for the given JDI type info.
   *
   * @param _type The JDI type with which to wrap in a type info profile
   * @return The new type info profile
   */
  def `type`(_type: Type): TypeInfoProfile

  /**
   * Retrieves a value info profile for the given JDI value info.
   *
   * @param value The JDI value with which to wrap in a value info profile
   * @return The new value info profile
   */
  def value(value: Value): ValueInfoProfile
}
