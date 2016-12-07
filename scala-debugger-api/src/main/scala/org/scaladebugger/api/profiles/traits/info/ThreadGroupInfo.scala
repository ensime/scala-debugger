package org.scaladebugger.api.profiles.traits.info


import com.sun.jdi.{ThreadGroupReference, ThreadReference}

import scala.util.Try

/**
 * Represents the interface for thread-based interaction.
 */
trait ThreadGroupInfo extends ObjectInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ThreadGroupInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadGroupReference

  /**
   * Represents the name of the thread group.
   *
   * @return The thread group name as a string
   */
  def name: String

  /**
   * Represents the parent of this thread group.
   *
   * @return Some thread group if a parent exists, otherwise None if top-level
   */
  def parent: Option[ThreadGroupInfo]

  /**
   * Resumes all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  def resume(): Unit

  /**
   * Suspends all threads in the thread group and subgroups. This is not an
   * atomic operation, so new threads added to a group will be unaffected.
   */
  def suspend(): Unit

  /**
   * Returns all live (started, but not stopped) threads in this thread group.
   * Does not include any threads in subgroups.
   *
   * @return The collection of threads
   */
  def threads: Seq[ThreadInfo]

  /**
   * Returns all live thread groups in this thread group. Only immediate
   * subgroups to this group are returned.
   *
   * @return The collection of thread groups
   */
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
