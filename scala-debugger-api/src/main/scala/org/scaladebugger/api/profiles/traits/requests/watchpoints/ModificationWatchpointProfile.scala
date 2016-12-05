package org.scaladebugger.api.profiles.traits.requests.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.watchpoints.ModificationWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ModificationWatchpointEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * modification watchpoint functionality for a specific debug profile.
 */
trait ModificationWatchpointProfile {
  /** Represents a modification watchpoint event and any associated data. */
  type ModificationWatchpointEventAndData =
    (ModificationWatchpointEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending modification watchpoint
   * requests.
   *
   * @return The collection of information on modification watchpoint requests
   */
  def modificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo]

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events
   */
  def tryGetOrCreateModificationWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventInfoProfile]] = {
    tryGetOrCreateModificationWatchpointRequestWithData(
      className: String,
      fieldName: String,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def tryGetOrCreateModificationWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventAndData]]

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events
   */
  def getOrCreateModificationWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventInfoProfile] = {
    tryGetOrCreateModificationWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def getOrCreateModificationWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] = {
    tryGetOrCreateModificationWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    ).get
  }

  /**
   * Determines if there is any modification watchpoint request for the
   * specified class field that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param fieldName The name of the field being watched
   * @return True if there is at least one modification watchpoint request with
   *         the specified field name in the specified class that is pending,
   *         otherwise false
   */
  def isModificationWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Determines if there is any modification watchpoint request for the
   * specified class field with matching arguments that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       modification watchpoint request
   * @return True if there is at least one modification watchpoint request with
   *         the specified field name and arguments in the specified class that
   *         is pending, otherwise false
   */
  def isModificationWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all modification watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return The collection of information about removed modification watchpoint requests
   */
  def removeModificationWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[ModificationWatchpointRequestInfo]

  /**
   * Removes all modification watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return Success containing the collection of information about removed
   *         modification watchpoint requests, otherwise a failure
   */
  def tryRemoveModificationWatchpointRequests(
    className: String,
    fieldName: String
  ): Try[Seq[ModificationWatchpointRequestInfo]] = Try(removeModificationWatchpointRequests(
    className,
    fieldName
  ))

  /**
   * Removes all modification watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       modification watchpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeModificationWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[ModificationWatchpointRequestInfo]

  /**
   * Removes all modification watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       modification watchpoint request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveModificationWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[Option[ModificationWatchpointRequestInfo]] = Try(removeModificationWatchpointRequestWithArgs(
    className,
    fieldName,
    extraArguments: _*
  ))

  /**
   * Removes all modification watchpoint requests.
   *
   * @return The collection of information about removed modification watchpoint requests
   */
  def removeAllModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo]

  /**
   * Removes all modification watchpoint requests.
   *
   * @return Success containing the collection of information about removed
   *         modification watchpoint requests, otherwise a failure
   */
  def tryRemoveAllModificationWatchpointRequests(): Try[Seq[ModificationWatchpointRequestInfo]] = Try(
    removeAllModificationWatchpointRequests()
  )
}
