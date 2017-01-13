package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ThreadGroupInfo, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a thread group profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            thread group
 * @param infoProducer The producer of info-based profile instances
 * @param _threadGroupReference The reference to the underlying JDI thread group
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _referenceType The reference type for this thread group
 */
class PureThreadGroupInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _threadGroupReference: ThreadGroupReference
)(
  override protected val _virtualMachine: VirtualMachine = _threadGroupReference.virtualMachine(),
  private val _referenceType: ReferenceType = _threadGroupReference.referenceType()
) extends PureObjectInfo(scalaVirtualMachine, infoProducer, _threadGroupReference)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with ThreadGroupInfo {
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
  override def toJavaInfo: ThreadGroupInfo = {
    infoProducer.toJavaInfo.newThreadGroupInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      threadGroupReference = _threadGroupReference
    )(
      virtualMachine = _virtualMachine,
      referenceType = _referenceType
    )
  }

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
  override def parent: Option[ThreadGroupInfo] =
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
  override def threadGroups: Seq[ThreadGroupInfo] = {
    import scala.collection.JavaConverters._
    _threadGroupReference.threadGroups().asScala.map(newThreadGroupProfile)
  }

  /**
   * Returns all live (started, but not stopped) threads in this thread group.
   * Does not include any threads in subgroups.
   *
   * @return The collection of threads
   */
  override def threads: Seq[ThreadInfo] = {
    import scala.collection.JavaConverters._
    _threadGroupReference.threads().asScala.map(newThreadProfile)
  }

  override protected def newThreadGroupProfile(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = infoProducer.newThreadGroupInfo(
    scalaVirtualMachine,
    threadGroupReference
  )(
    virtualMachine = _virtualMachine,
    referenceType = _referenceType
  )

  override protected def newThreadProfile(
    threadReference: ThreadReference
  ): ThreadInfo = infoProducer.newThreadInfo(
    scalaVirtualMachine,
    threadReference
  )(
    virtualMachine = _virtualMachine,
    referenceType = _referenceType
  )
}
