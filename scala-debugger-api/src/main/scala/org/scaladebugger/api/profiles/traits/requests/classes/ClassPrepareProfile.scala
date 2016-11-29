package org.scaladebugger.api.profiles.traits.requests.classes
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassPrepareRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * class prepare functionality for a specific debug profile.
 */
trait ClassPrepareProfile {
  /** Represents a class prepare event and any associated data. */
  type ClassPrepareEventAndData = (ClassPrepareEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending class prepare requests.
   *
   * @return The collection of information on class prepare requests
   */
  def classPrepareRequests: Seq[ClassPrepareRequestInfo]

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events
   */
  def tryGetOrCreateClassPrepareRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventInfoProfile]] = {
    tryGetOrCreateClassPrepareRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events
   */
  def getOrCreateClassPrepareRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventInfoProfile] = {
    tryGetOrCreateClassPrepareRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventAndData] = {
    tryGetOrCreateClassPrepareRequestWithData(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]]

  /**
   * Determines if the class prepare request with the specified arguments
   * is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       class prepare request
   * @return True if there is at least one class prepare request with the
   *         provided extra arguments that is pending, otherwise false
   */
  def isClassPrepareRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all class prepare requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class prepare request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeClassPrepareRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ClassPrepareRequestInfo]

  /**
   * Removes all class prepare requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class prepare request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveClassPrepareRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[ClassPrepareRequestInfo]] = Try(removeClassPrepareRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all class prepare requests.
   *
   * @return The collection of information about removed class prepare requests
   */
  def removeAllClassPrepareRequests(): Seq[ClassPrepareRequestInfo]

  /**
   * Removes all class prepare requests.
   *
   * @return Success containing the collection of information about removed
   *         class prepare requests, otherwise a failure
   */
  def tryRemoveAllClassPrepareRequests(): Try[Seq[ClassPrepareRequestInfo]] = Try(
    removeAllClassPrepareRequests()
  )
}
