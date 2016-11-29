package org.scaladebugger.api.profiles.traits.requests.exceptions
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.exceptions.ExceptionRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * exception functionality for a specific debug profile.
 */
trait ExceptionProfile {
  /** Represents a exception event and any associated data. */
  type ExceptionEventAndData = (ExceptionEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending exception requests.
   *
   * @return The collection of information on exception requests
   */
  def exceptionRequests: Seq[ExceptionRequestInfo]

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events
   */
  def tryGetOrCreateExceptionRequest(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventInfoProfile]] = {
    tryGetOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events
   */
  def getOrCreateExceptionRequest(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventInfoProfile] = {
    tryGetOrCreateExceptionRequest(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateExceptionRequestWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] = {
    tryGetOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateExceptionRequestWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]]

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events
   */
  def tryGetOrCreateAllExceptionsRequest(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventInfoProfile]] = {
    tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events
   */
  def getOrCreateAllExceptionsRequest(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventInfoProfile] = {
    tryGetOrCreateAllExceptionsRequest(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateAllExceptionsRequestWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]]

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateAllExceptionsRequestWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] = {
    tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).get
  }

  /**
   * Determines if there is any "all exceptions" request pending.
   *
   * @return True if there is at least one "all exceptions" request pending,
   *         otherwise false
   */
  def isAllExceptionsRequestPending: Boolean

  /**
   * Determines if there is any "all exceptions" request pending with the
   * specified arguments.
   *
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments The additional arguments provided to the specific
   *                       exception request
   * @return True if there is at least one "all exceptions" request with the
   *         specified notify caught, notify uncaught, and extra arguments that
   *         is pending, otherwise false
   */
  def isAllExceptionsRequestWithArgsPending(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Determines if there is any exception with the specified class name that
   * is pending.
   *
   * @param exceptionName The full class name of the exception
   * @return True if there is at least one exception with the specified class
   *         name that is pending, otherwise false
   */
  def isExceptionRequestPending(exceptionName: String): Boolean

  /**
   * Determines if there is any exception with the specified class name that
   * is pending.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments The additional arguments provided to the specific
   *                       exception request
   * @return True if there is at least one exception with the specified class
   *         name, notify caught, notify uncaught, and extra arguments that is
   *         pending, otherwise false
   */
  def isExceptionRequestWithArgsPending(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes exception requests targeted towards "all exceptions."
   *
   * @return The collection of information about removed exception requests
   */
  def removeOnlyAllExceptionsRequests(): Seq[ExceptionRequestInfo]

  /**
   * Removes exception requests targeted towards "all exceptions."
   *
   * @return Success containing the collection of information about removed
   *         exception requests, otherwise a failure
   */
  def tryRemoveOnlyAllExceptionsRequests(): Try[Seq[ExceptionRequestInfo]] =
    Try(removeOnlyAllExceptionsRequests())

  /**
   * Removes the exception request targeted towards "all exceptions" with
   * the specified notification flags and extra arguments.
   *
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeOnlyAllExceptionsRequestWithArgs(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo]

  /**
   * Removes the exception request targeted towards "all exceptions" with
   * the specified notification flags and extra arguments.
   *
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveOnlyAllExceptionsRequestWithArgs(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[Option[ExceptionRequestInfo]] = Try(removeOnlyAllExceptionsRequestWithArgs(
    notifyCaught,
    notifyUncaught,
    extraArguments: _*
  ))

  /**
   * Removes all exception requests with the specified class name.
   *
   * @param exceptionName The full class name of the exception
   * @return The collection of information about removed exception requests
   */
  def removeExceptionRequests(exceptionName: String): Seq[ExceptionRequestInfo]

  /**
   * Removes all exception requests with the specified class name.
   *
   * @param exceptionName The full class name of the exception
   * @return Success containing the collection of information about removed
   *         exception requests, otherwise a failure
   */
  def tryRemoveExceptionRequests(
    exceptionName: String
  ): Try[Seq[ExceptionRequestInfo]] = Try(removeExceptionRequests(
    exceptionName
  ))

  /**
   * Remove the exception request with the specified class name, notification
   * flags, and extra arguments.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeExceptionRequestWithArgs(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo]

  /**
   * Remove the exception request with the specified class name, notification
   * flags, and extra arguments.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveExceptionRequestWithArgs(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[Option[ExceptionRequestInfo]] = Try(removeExceptionRequestWithArgs(
    exceptionName,
    notifyCaught,
    notifyUncaught,
    extraArguments: _*
  ))

  /**
   * Removes all exception requests.
   *
   * @return The collection of information about removed exception requests
   */
  def removeAllExceptionRequests(): Seq[ExceptionRequestInfo]

  /**
   * Removes all exception requests.
   *
   * @return Success containing the collection of information about removed
   *         exception requests, otherwise a failure
   */
  def tryRemoveAllExceptionRequests(): Try[Seq[ExceptionRequestInfo]] = Try(
    removeAllExceptionRequests()
  )
}
