package org.scaladebugger.api.profiles.traits.methods
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.methods.MethodExitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * method exit functionality for a specific debug profile.
 */
trait MethodExitProfile {
  /** Represents a method exit event and any associated data. */
  type MethodExitEventAndData = (MethodExitEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending method exit requests.
   *
   * @return The collection of information on method exit requests
   */
  def methodExitRequests: Seq[MethodExitRequestInfo]

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events
   */
  def tryGetOrCreateMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventInfoProfile]] = {
    tryGetOrCreateMethodExitRequestWithData(
      className: String,
      methodName: String,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events
   */
  def getOrCreateMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventInfoProfile] = {
    tryGetOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventAndData] = {
    tryGetOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]]

  /**
   * Determines if there is any method exit request for the specified class
   * method that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return True if there is at least one method exit request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  def isMethodExitRequestPending(
    className: String,
    methodName: String
  ): Boolean

  /**
   * Determines if there is any method exit request for the specified class
   * method with matching arguments that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       method exit request
   * @return True if there is at least one method exit request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isMethodExitRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all method exit requests for the specified class method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return The collection of information about removed method exit requests
   */
  def removeMethodExitRequests(
    className: String,
    methodName: String
  ): Seq[MethodExitRequestInfo]

  /**
   * Removes all method exit requests for the specified class method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return Success containing the collection of information about removed
   *         method exit requests, otherwise a failure
   */
  def tryRemoveMethodExitRequests(
    className: String,
    methodName: String
  ): Try[Seq[MethodExitRequestInfo]] = Try(removeMethodExitRequests(
    className,
    methodName
  ))

  /**
   * Removes all method exit requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method exit request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMethodExitRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodExitRequestInfo]

  /**
   * Removes all method exit requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method exit request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMethodExitRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[Option[MethodExitRequestInfo]] = Try(removeMethodExitRequestWithArgs(
    className,
    methodName,
    extraArguments: _*
  ))

  /**
   * Removes all method exit requests.
   *
   * @return The collection of information about removed method exit requests
   */
  def removeAllMethodExitRequests(): Seq[MethodExitRequestInfo]

  /**
   * Removes all method exit requests.
   *
   * @return Success containing the collection of information about removed
   *         method exit requests, otherwise a failure
   */
  def tryRemoveAllMethodExitRequests(): Try[Seq[MethodExitRequestInfo]] = Try(
    removeAllMethodExitRequests()
  )
}
