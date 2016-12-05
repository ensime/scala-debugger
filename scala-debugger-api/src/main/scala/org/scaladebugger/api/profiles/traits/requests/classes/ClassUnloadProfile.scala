package org.scaladebugger.api.profiles.traits.requests.classes

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassUnloadRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * class unload functionality for a specific debug profile.
 */
trait ClassUnloadProfile {
  /** Represents a class unload event and any associated data. */
  type ClassUnloadEventAndData = (ClassUnloadEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending class unload requests.
   *
   * @return The collection of information on class unload requests
   */
  def classUnloadRequests: Seq[ClassUnloadRequestInfo]

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events
   */
  def tryGetOrCreateClassUnloadRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventInfoProfile]] = {
    tryGetOrCreateClassUnloadRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events
   */
  def getOrCreateClassUnloadRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventInfoProfile] = {
    tryGetOrCreateClassUnloadRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateClassUnloadRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventAndData] = {
    tryGetOrCreateClassUnloadRequestWithData(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateClassUnloadRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventAndData]]

  /**
   * Determines if the class unload request with the specified arguments
   * is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       class unload request
   * @return True if there is at least one class unload request with the
   *         provided extra arguments that is pending, otherwise false
   */
  def isClassUnloadRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all class unload requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class unload request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeClassUnloadRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ClassUnloadRequestInfo]

  /**
   * Removes all class unload requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class unload request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveClassUnloadRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[ClassUnloadRequestInfo]] = Try(removeClassUnloadRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all class unload requests.
   *
   * @return The collection of information about removed class unload requests
   */
  def removeAllClassUnloadRequests(): Seq[ClassUnloadRequestInfo]

  /**
   * Removes all class unload requests.
   *
   * @return Success containing the collection of information about removed
   *         class unload requests, otherwise a failure
   */
  def tryRemoveAllClassUnloadRequests(): Try[Seq[ClassUnloadRequestInfo]] = Try(
    removeAllClassUnloadRequests()
  )
}
