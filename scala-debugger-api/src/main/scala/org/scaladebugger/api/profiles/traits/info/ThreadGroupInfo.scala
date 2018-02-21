package org.scaladebugger.api.profiles.traits.info


import acyclic.file
import com.sun.jdi.ThreadGroupReference
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

/**
 * Represents the interface for thread-based interaction.
 */
//@Freezable
trait ThreadGroupInfo extends ObjectInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ThreadGroupInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: ThreadGroupReference

  /**
   * Represents the name of the thread group.
   *
   * @return The thread group name as a string
   */
  @CanFreeze
  def name: String

  /**
   * Represents the parent of this thread group.
   *
   * @return Some thread group if a parent exists, otherwise None if top-level
   */
  @CanFreeze(ReturnType.FreezeOption)
  def parent: Option[ThreadGroupInfo]

  /**
   * Resumes all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  @CannotFreeze
  def resume(): Unit

  /**
   * Suspends all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  @CannotFreeze
  def suspend(): Unit

  /**
   * Returns all live (started, but not stopped) threads in this thread group.
   * Does not include any threads in subgroups.
   *
   * @return The collection of threads
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def threads: Seq[ThreadInfo]

  /**
   * Returns all live thread groups in this thread group. Only immediate
   * subgroups to this group are returned.
   *
   * @return The collection of thread groups
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def threadGroups: Seq[ThreadGroupInfo]

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    s"Thread Group $name (0x$uniqueIdHexString)"
  }
}
