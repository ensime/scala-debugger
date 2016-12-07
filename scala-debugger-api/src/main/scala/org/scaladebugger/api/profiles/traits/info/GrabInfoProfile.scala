package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi._
import java.util.NoSuchElementException

import scala.annotation.tailrec
import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * the ability to grab various information for a specific debug profile.
 */
trait GrabInfoProfile {
  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return Success containing the object profile, otherwise a failure
   */
  def tryObject(objectReference: ObjectReference): Try[ObjectInfo] =
    Try(`object`(objectReference))

  /**
   * Retrieves a object profile for the given JDI object reference.
   *
   *                   object
 *
   * @param objectReference The JDI object reference with which to wrap in
   *                        a object info profile
   * @return The new object info profile
   */
  def `object`(objectReference: ObjectReference): ObjectInfo =
    `object`(objectReference)

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return Success containing the collection of thread info profiles,
   *         otherwise a failure
   */
  def tryThreads: Try[Seq[ThreadInfo]] = Try(threads)

  /**
   * Retrieves all threads contained in the remote JVM.
   *
   * @return The collection of thread info profiles
   */
  def threads: Seq[ThreadInfo]

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return Success containing the thread profile, otherwise a failure
   */
  def tryThread(threadReference: ThreadReference): Try[ThreadInfo] =
    Try(thread(threadReference))

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return The new thread info profile
   */
  def thread(threadReference: ThreadReference): ThreadInfo

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name.
   *
   * @param name The name of the thread
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryThread(name: String): Try[ThreadInfo] =
    Try(thread(name))

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name.
   *
   * @param name The name of the thread
   * @return The profile of the matching thread, or throws an exception
   */
  def thread(name: String): ThreadInfo = {
    val t = threadOption(name)

    if (t.isEmpty)
      throw new NoSuchElementException(s"No thread named $name found!")

    t.get
  }

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name.
   *
   * @param name The name of the thread
   * @return Some profile of the matching thread, or None
   */
  def threadOption(name: String): Option[ThreadInfo] = {
    threads.find(_.name == name)
  }

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name and whose thread group has the specified name.
   *
   * @param threadName The name of the thread
   * @param threadGroupName The name of the thread group
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryThread(
    threadName: String,
    threadGroupName: String
  ): Try[ThreadInfo] = Try(thread(
    threadGroupName = threadGroupName,
    threadName = threadName
  ))

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name and whose thread group has the specified name.
   *
   * @param threadName The name of the thread
   * @param threadGroupName The name of the thread group
   * @return The profile of the matching thread, or throws an exception
   */
  def thread(
    threadName: String,
    threadGroupName: String
  ): ThreadInfo = {
    val t = threadOption(
      threadGroupName = threadGroupName,
      threadName = threadName
    )

    if (t.isEmpty) throw new NoSuchElementException(
      s"No thread named $threadName with thread group $threadGroupName found!"
    )

    t.get
  }

  /**
   * Retrieves a thread profile for the thread reference whose name matches
   * the provided name and whose thread group has the specified name.
   *
   * @param threadName The name of the thread
   * @param threadGroupName The name of the thread group
   * @return Some profile of the matching thread, or None
   */
  def threadOption(
    threadName: String,
    threadGroupName: String
  ): Option[ThreadInfo] = {
    threads.find(t =>
      t.name == threadName && t.threadGroup.name == threadGroupName
    )
  }

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryThread(threadId: Long): Try[ThreadInfo] =
    Try(thread(threadId))

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return The profile of the matching thread, or throws an exception
   */
  def thread(threadId: Long): ThreadInfo = {
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
  def threadOption(threadId: Long): Option[ThreadInfo] = {
    threads.find(_.uniqueId == threadId)
  }

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
  ): Try[ThreadGroupInfo] = Try(threadGroup(threadGroupReference))

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
  ): ThreadGroupInfo

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupId The id of the thread group
   * @return Success containing the thread group profile if found, otherwise
   *         a failure
   */
  def tryThreadGroup(threadGroupId: Long): Try[ThreadGroupInfo] =
    Try(threadGroup(threadGroupId))

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * unique id matches the provided id.
   *
   * @param threadGroupId The id of the thread group
   * @return The profile of the matching thread group, or throws an exception
   */
  def threadGroup(threadGroupId: Long): ThreadGroupInfo = {
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
  def threadGroupOption(
    threadGroupId: Long
  ): Option[ThreadGroupInfo] = {
    findThreadGroupByPredicate(threadGroups, _.uniqueId == threadGroupId)
  }

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * name matches the provided name.
   *
   * @param name The name of the thread group
   * @return Success containing the thread group profile if found, otherwise
   *         a failure
   */
  def tryThreadGroup(name: String): Try[ThreadGroupInfo] =
    Try(threadGroup(name))

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * name matches the provided name.
   *
   * @param name The name of the thread group
   * @return The profile of the matching thread group, or throws an exception
   */
  def threadGroup(name: String): ThreadGroupInfo = {
    val tg = threadGroupOption(name)

    if (tg.isEmpty) throw new NoSuchElementException(
      s"No thread group named $name found!")

    tg.get
  }

  /**
   * Retrieves a thread group profile for the thread group reference whose
   * name matches the provided name.
   *
   * @param name The name of the thread group
   * @return Some profile of the matching thread group, or None
   */
  def threadGroupOption(
    name: String
  ): Option[ThreadGroupInfo] = {
    findThreadGroupByPredicate(threadGroups, _.name == name)
  }

  /**
   * Recursively searches a collection of thread groups (and their subgroups)
   * for a thread group that satisfies the predicate.
   *
   * @param threadGroups The initial collection of thread groups to search
   * @param predicate The predicate used to find a matching thread group
   * @return Some thread group if found, otherwise None
   */
  @tailrec private def findThreadGroupByPredicate(
    threadGroups: Seq[ThreadGroupInfo],
    predicate: ThreadGroupInfo => Boolean
  ): Option[ThreadGroupInfo] = {
    if (threadGroups.nonEmpty) {
      val tg = threadGroups.find(predicate)
      if (tg.nonEmpty) {
        tg
      } else {
        findThreadGroupByPredicate(
          threadGroups.flatMap(_.threadGroups),
          predicate
        )
      }
    } else {
      None
    }
  }

  /**
   * Retrieves all thread groups contained in the remote JVM.
   *
   * @return Success containing the collection of thread group info profiles,
   *         otherwise a failure
   */
  def tryThreadGroups: Try[Seq[ThreadGroupInfo]] = Try(threadGroups)

  /**
   * Retrieves all thread groups contained in the remote JVM.
   *
   * @return The collection of thread group info profiles
   */
  def threadGroups: Seq[ThreadGroupInfo]

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return Success containing the collection of reference type info profiles,
   *         otherwise a failure
   */
  def tryClasses: Try[Seq[ReferenceTypeInfo]] = Try(classes)

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  def classes: Seq[ReferenceTypeInfo]

  /**
   * Retrieves a reference type profile for the given JDI reference type.
   *
   * @return The reference type info profile wrapping the JDI instance
   */
  def `class`(referenceType: ReferenceType): ReferenceTypeInfo

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return Success containing the reference type info profile for the class,
   *         otherwise a failure
   */
  def tryClass(name: String): Try[ReferenceTypeInfo] = Try(`class`(name))

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return The reference type info profile for the class
   */
  def `class`(name: String): ReferenceTypeInfo = {
    val c = classOption(name)

    if (c.isEmpty)
      throw new NoSuchElementException(s"Class with name '$name' not found!")

    c.get
  }

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @return Some reference type info profile for the class if found,
   *         otherwise None
   */
  def classOption(name: String): Option[ReferenceTypeInfo] = {
    classes.find(_.name == name)
  }

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
  ): Try[FieldVariableInfo] = Try(this.field(referenceType, field))

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
  ): FieldVariableInfo

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
    referenceTypeInfo: ReferenceTypeInfo,
    field: Field
  ): Try[FieldVariableInfo] = Try(this.field(referenceTypeInfo, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceTypeInfo The information about the reference type to
   *                          associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    referenceTypeInfo: ReferenceTypeInfo,
    field: Field
  ): FieldVariableInfo =
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
  ): Try[FieldVariableInfo] = Try(this.field(objectReference, field))

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
  ): FieldVariableInfo

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
    objectInfo: ObjectInfo,
    field: Field
  ): Try[FieldVariableInfo] = Try(this.field(objectInfo, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param objectInfo The information about the object to associate with
   *                   the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile representing the field
   */
  def field(
    objectInfo: ObjectInfo,
    field: Field
  ): FieldVariableInfo = this.field(objectInfo.toJdiInstance, field)

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
  ): Try[VariableInfo] = Try(this.localVariable(
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
  ): VariableInfo

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
    stackFrameInfo: FrameInfo,
    localVariable: LocalVariable
  ): Try[VariableInfo] = Try(this.localVariable(
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
    stackFrameInfo: FrameInfo,
    localVariable: LocalVariable
  ): VariableInfo = this.localVariable(
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
  def location(location: Location): LocationInfo

  /**
   * Retrieves a method profile for the given JDI method.
   *
   * @param method The JDI method with which to wrap in a method info profile
   * @return The new method info profile
   */
  def method(method: Method): MethodInfo

  /**
   * Retrieves a stack frame profile for the given JDI stack frame.
   *
   * @param stackFrame The JDI stack frame with which to wrap in a
   *                   frame info profile
   * @return The new frame info profile
   */
  def stackFrame(stackFrame: StackFrame): FrameInfo

  /**
   * Retrieves a type info profile for the given JDI type info.
   *
   * @param _type The JDI type with which to wrap in a type info profile
   * @return The new type info profile
   */
  def `type`(_type: Type): TypeInfo

  /**
   * Retrieves a value info profile for the given JDI value info.
   *
   * @param value The JDI value with which to wrap in a value info profile
   * @return The new value info profile
   */
  def value(value: Value): ValueInfo
}
