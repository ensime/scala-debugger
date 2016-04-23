package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi.{ObjectReference, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.profiles.traits.info.{GrabInfoProfile, ObjectInfoProfile, ReferenceTypeInfoProfile, ThreadInfoProfile}
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

  protected def newThreadProfile(threadReference: ThreadReference): ThreadInfoProfile =
    new PureThreadInfoProfile(
      scalaVirtualMachine,
      threadReference
    )(_virtualMachine = _virtualMachine)

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  protected def newObjectProfile(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile = new PureObjectInfoProfile(
    scalaVirtualMachine,
    objectReference
  )(
    _threadReference = threadReference
  )
}
