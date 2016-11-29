package org.scaladebugger.api.profiles.traits.requests.methods
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.methods.MethodEntryRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * method entry functionality for a specific debug profile.
 */
trait MethodEntryProfile {
  /** Represents a method entry event and any associated data. */
  type MethodEntryEventAndData = (MethodEntryEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending method entry requests.
   *
   * @return The collection of information on method entry requests
   */
  def methodEntryRequests: Seq[MethodEntryRequestInfo]

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events
   */
  def tryGetOrCreateMethodEntryRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventInfoProfile]] = {
    tryGetOrCreateMethodEntryRequestWithData(
      className: String,
      methodName: String,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events
   */
  def getOrCreateMethodEntryRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodEntryEventInfoProfile] = {
    tryGetOrCreateMethodEntryRequest(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateMethodEntryRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodEntryEventAndData] = {
    tryGetOrCreateMethodEntryRequestWithData(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateMethodEntryRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]]

  /**
   * Determines if there is any method entry request for the specified class
   * method that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return True if there is at least one method entry request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  def isMethodEntryRequestPending(
    className: String,
    methodName: String
  ): Boolean

  /**
   * Determines if there is any method entry request for the specified class
   * method with matching arguments that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       method entry request
   * @return True if there is at least one method entry request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isMethodEntryRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all method entry requests for the specified class method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return The collection of information about removed method entry requests
   */
  def removeMethodEntryRequests(
    className: String,
    methodName: String
  ): Seq[MethodEntryRequestInfo]

  /**
   * Removes all method entry requests for the specified class method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return Success containing the collection of information about removed
   *         method entry requests, otherwise a failure
   */
  def tryRemoveMethodEntryRequests(
    className: String,
    methodName: String
  ): Try[Seq[MethodEntryRequestInfo]] = Try(removeMethodEntryRequests(
    className,
    methodName
  ))

  /**
   * Removes all method entry requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method entry request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMethodEntryRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodEntryRequestInfo]

  /**
   * Removes all method entry requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method entry request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMethodEntryRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[Option[MethodEntryRequestInfo]] = Try(removeMethodEntryRequestWithArgs(
    className,
    methodName,
    extraArguments: _*
  ))

  /**
   * Removes all method entry requests.
   *
   * @return The collection of information about removed method entry requests
   */
  def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo]

  /**
   * Removes all method entry requests.
   *
   * @return Success containing the collection of information about removed
   *         method entry requests, otherwise a failure
   */
  def tryRemoveAllMethodEntryRequests(): Try[Seq[MethodEntryRequestInfo]] = Try(
    removeAllMethodEntryRequests()
  )
}
