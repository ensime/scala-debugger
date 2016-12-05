package org.scaladebugger.api.profiles.traits.requests.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.watchpoints.AccessWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.events.AccessWatchpointEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * access watchpoint functionality for a specific debug profile.
 */
trait AccessWatchpointProfile {
  /** Represents a access watchpoint event and any associated data. */
  type AccessWatchpointEventAndData =
    (AccessWatchpointEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending access watchpoint requests.
   *
   * @return The collection of information on access watchpoint requests
   */
  def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo]

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events
   */
  def tryGetOrCreateAccessWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventInfoProfile]] = {
    tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def tryGetOrCreateAccessWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]]

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events
   */
  def getOrCreateAccessWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEventInfoProfile] = {
    tryGetOrCreateAccessWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def getOrCreateAccessWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEventAndData] = {
    tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    ).get
  }

  /**
   * Determines if there is any access watchpoint request for the specified
   * class field that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return True if there is at least one access watchpoint request with the
   *         specified field name in the specified class that is pending,
   *         otherwise false
   */
  def isAccessWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Determines if there is any access watchpoint request for the specified
   * class field with matching arguments that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       access watchpoint request
   * @return True if there is at least one access watchpoint request with the
   *         specified field name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isAccessWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all access watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return The collection of information about removed access watchpoint requests
   */
  def removeAccessWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[AccessWatchpointRequestInfo]

  /**
   * Removes all access watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return Success containing the collection of information about removed
   *         access watchpoint requests, otherwise a failure
   */
  def tryRemoveAccessWatchpointRequests(
    className: String,
    fieldName: String
  ): Try[Seq[AccessWatchpointRequestInfo]] = Try(removeAccessWatchpointRequests(
    className,
    fieldName
  ))

  /**
   * Removes all access watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       access watchpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeAccessWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[AccessWatchpointRequestInfo]

  /**
   * Removes all access watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       access watchpoint request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveAccessWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[Option[AccessWatchpointRequestInfo]] = Try(removeAccessWatchpointRequestWithArgs(
    className,
    fieldName,
    extraArguments: _*
  ))

  /**
   * Removes all access watchpoint requests.
   *
   * @return The collection of information about removed access watchpoint requests
   */
  def removeAllAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo]

  /**
   * Removes all access watchpoint requests.
   *
   * @return Success containing the collection of information about removed
   *         access watchpoint requests, otherwise a failure
   */
  def tryRemoveAllAccessWatchpointRequests(): Try[Seq[AccessWatchpointRequestInfo]] = Try(
    removeAllAccessWatchpointRequests()
  )
}
