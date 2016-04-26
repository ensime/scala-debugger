package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.{Field, ObjectReference, ReferenceType, ThreadReference}

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
  def thread(threadId: Long): ThreadInfoProfile = threadOption(threadId).get

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Some profile of the matching thread, or None
   */
  def threadOption(threadId: Long): Option[ThreadInfoProfile]

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
  def `class`(name: String): ReferenceTypeInfoProfile = classOption(name).get

  /**
   * Retrieves reference information for the class with the specified name.
   *
   * @param name The fully-qualified name of the class
   * @return Some reference type info profile for the class if found,
   *         otherwise None
   */
  def classOption(name: String): Option[ReferenceTypeInfoProfile]

  // ==========================================================================

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceType The reference type to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return Success containing the variable profile, otherwise a failure
   */
  def tryField(
    referenceType: ReferenceType,
    field: Field
  ): Try[VariableInfoProfile] = Try(this.field(referenceType, field))

  /**
   * Retrieves a field profile for the given JDI field.
   *
   * @param referenceType The reference type to associate with the field
   * @param field The JDI field with which to wrap in a variable info profile
   * @return The variable profile
   */
  def field(referenceType: ReferenceType, field: Field): VariableInfoProfile

  def tryField(
    referenceTypeInfo: ReferenceTypeInfoProfile,
    field: Field
  ): Try[VariableInfoProfile] = Try(this.field(referenceTypeInfo, field))

  def field(
    referenceTypeInfo: ReferenceTypeInfoProfile,
    field: Field
  ): VariableInfoProfile = this.field(referenceTypeInfo.toJdiInstance, field)

  // ==========================================================================

}
