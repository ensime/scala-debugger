package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ThreadGroupInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a thread group profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            thread group
 * @param _threadGroupReference The reference to the underlying JDI thread group
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _threadReference The thread containing this thread group object
 * @param _referenceType The reference type for this thread group
 */
class PureThreadGroupInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _threadGroupReference: ThreadGroupReference
)(
  override protected val _virtualMachine: VirtualMachine = _threadGroupReference.virtualMachine(),
  private val _threadReference: ThreadReference = _threadGroupReference.owningThread(),
  private val _referenceType: ReferenceType = _threadGroupReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, _threadGroupReference)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) with ThreadGroupInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadGroupReference = _threadGroupReference

  /**
   * Represents the name of the thread group.
   *
   * @return The thread group name as a string
   */
  override def name: String = _threadGroupReference.name()

  /**
   * Represents the parent of this thread group.
   *
   * @return Some thread group if a parent exists, otherwise None if top-level
   */
  override def parent: Option[ThreadGroupInfoProfile] =
    Option(_threadGroupReference.parent()).map(newThreadGroupProfile)

  /**
   * Suspends all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  override def suspend(): Unit = _threadGroupReference.suspend()

  /**
   * Resumes all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  override def resume(): Unit = _threadGroupReference.resume()

  /**
   * Returns all live thread groups in this thread group. Only immediate
   * subgroups to this group are returned.
   *
   * @return The collection of thread groups
   */
  override def threadGroups: Seq[ThreadGroupInfoProfile] = {
    import scala.collection.JavaConverters._
    _threadGroupReference.threadGroups().asScala.map(newThreadGroupProfile)
  }

  /**
   * Returns all live (started, but not stopped) threads in this thread group.
   * Does not include any threads in subgroups.
   *
   * @return The collection of threads
   */
  override def threads: Seq[ThreadInfoProfile] = {
    import scala.collection.JavaConverters._
    _threadGroupReference.threads().asScala.map(newThreadProfile)
  }

  protected def newThreadGroupProfile(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfoProfile = new PureThreadGroupInfoProfile(
    scalaVirtualMachine,
    threadGroupReference
  )(
    _virtualMachine = _virtualMachine,
    _threadReference = _threadReference,
    _referenceType = _referenceType
  )

  protected def newThreadProfile(
    threadReference: ThreadReference
  ): ThreadInfoProfile = new PureThreadInfoProfile(
    scalaVirtualMachine,
    threadReference
  )(
    _virtualMachine = _virtualMachine,
    _referenceType = _referenceType
  )
}
