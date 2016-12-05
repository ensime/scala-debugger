package org.scaladebugger.api.profiles.traits.requests.threads

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.threads.ThreadStartRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadStartEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * thread start functionality for a specific debug profile.
 */
trait ThreadStartProfile {
  /** Represents a thread start event and any associated data. */
  type ThreadStartEventAndData =
    (ThreadStartEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending thread start requests.
   *
   * @return The collection of information on thread start requests
   */
  def threadStartRequests: Seq[ThreadStartRequestInfo]

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events
   */
  def tryGetOrCreateThreadStartRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventInfoProfile]] = {
    tryGetOrCreateThreadStartRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateThreadStartRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]]

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events
   */
  def getOrCreateThreadStartRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEventInfoProfile] = {
    tryGetOrCreateThreadStartRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateThreadStartRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEventAndData] = {
    tryGetOrCreateThreadStartRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the thread start request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       thread start request
   * @return True if there is at least one thread start request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isThreadStartRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all thread start requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread start request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeThreadStartRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadStartRequestInfo]

  /**
   * Removes all thread start requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread start request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveThreadStartRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[ThreadStartRequestInfo]] = Try(removeThreadStartRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all thread start requests.
   *
   * @return The collection of information about removed thread start requests
   */
  def removeAllThreadStartRequests(): Seq[ThreadStartRequestInfo]

  /**
   * Removes all thread start requests.
   *
   * @return Success containing the collection of information about removed
   *         thread start requests, otherwise a failure
   */
  def tryRemoveAllThreadStartRequests(): Try[Seq[ThreadStartRequestInfo]] = Try(
    removeAllThreadStartRequests()
  )
}
