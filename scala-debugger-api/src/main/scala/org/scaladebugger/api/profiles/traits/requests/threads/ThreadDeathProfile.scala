package org.scaladebugger.api.profiles.traits.requests.threads

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.threads.ThreadDeathRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadDeathEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * thread death functionality for a specific debug profile.
 */
trait ThreadDeathProfile {
  /** Represents a thread death event and any associated data. */
  type ThreadDeathEventAndData =
    (ThreadDeathEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending thread death requests.
   *
   * @return The collection of information on thread death requests
   */
  def threadDeathRequests: Seq[ThreadDeathRequestInfo]

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events
   */
  def tryGetOrCreateThreadDeathRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventInfoProfile]] = {
    tryGetOrCreateThreadDeathRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateThreadDeathRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]]

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events
   */
  def getOrCreateThreadDeathRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEventInfoProfile] = {
    tryGetOrCreateThreadDeathRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateThreadDeathRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEventAndData] = {
    tryGetOrCreateThreadDeathRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the thread death request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       thread death request
   * @return True if there is at least one thread death request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isThreadDeathRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all thread death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread death request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeThreadDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadDeathRequestInfo]

  /**
   * Removes all thread death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread death request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveThreadDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[ThreadDeathRequestInfo]] = Try(removeThreadDeathRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all thread death requests.
   *
   * @return The collection of information about removed thread death requests
   */
  def removeAllThreadDeathRequests(): Seq[ThreadDeathRequestInfo]

  /**
   * Removes all thread death requests.
   *
   * @return Success containing the collection of information about removed
   *         thread death requests, otherwise a failure
   */
  def tryRemoveAllThreadDeathRequests(): Try[Seq[ThreadDeathRequestInfo]] = Try(
    removeAllThreadDeathRequests()
  )
}
