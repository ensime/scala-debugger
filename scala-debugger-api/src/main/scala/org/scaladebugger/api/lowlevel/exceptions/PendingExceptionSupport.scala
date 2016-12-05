package org.scaladebugger.api.lowlevel.exceptions

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending exception capabilities to an existing exception manager.
 */
trait PendingExceptionSupport extends PendingExceptionSupportLike {
  /**
   * Represents the manager used to store pending exception requests and process
   * them later.
   */
  protected val pendingActionManager: PendingActionManager[ExceptionRequestInfo]

  /**
   * Processes all pending exception requests.
   *
   * @return The collection of successfully-processed exception requests
   */
  override def processAllPendingExceptionRequests(): Seq[ExceptionRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending exception requests.
   *
   * @return The collection of exception request information
   */
  override def pendingExceptionRequests: Seq[ExceptionRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending exception requests for the specified file.
   *
   * @param className The full name of the exception class whose pending
   *                  exception requests to process
   *
   * @return The collection of successfully-processed exception requests
   */
  override def processPendingExceptionRequestsForClass(
    className: String
  ): Seq[ExceptionRequestInfo] = {
    pendingActionManager.processActions(_.data.className == className)
      .map(_.data)
  }

  /**
   * Retrieves a list of pending exception requests for the specified file.
   *
   * @param className The full name of the exception class whose pending
   *                  exception requests to retrieve
   *
   * @return The collection of successfully-processed exception requests
   */
  override def pendingExceptionRequestsForClass(
    className: String
  ): Seq[ExceptionRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.className == className)
  }

  /**
   * Creates a new exception request to catch all exceptions from the JVM.
   *
   * @note The request id given does not get added to the request id list and
   *       removing by id will not remove this request instance.
   *
   * @param requestId The id associated with the requests for lookup and removal
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createCatchallExceptionRequestWithId(
    requestId: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createException() = super.createCatchallExceptionRequestWithId(
      requestId,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

    val result = createException()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ExceptionRequestInfo(
            requestId,
            isPending = true,
            ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught,
            notifyUncaught,
            extraArguments
          ),
          () => createException().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Creates a new exception request for the specified exception class.
   *
   * @note Any exception and its subclass will be watched.
   *
   * @param requestId The id associated with the requests for lookup and removal
   * @param exceptionName The full class name of the exception to watch
   * @param notifyCaught If true, events will be reported when the exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when the exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createExceptionRequestWithId(
    requestId: String,
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    require(exceptionName != null, "Exception name cannot be null!")
    def createException() = super.createExceptionRequestWithId(
      requestId,
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

    val result = createException()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ExceptionRequestInfo(
            requestId,
            isPending = true,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            extraArguments
          ),
          () => createException().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return True if the exception requests were removed (if they existed),
   *         otherwise false
   */
  abstract override def removeExceptionRequest(
    exceptionName: String
  ): Boolean = {
    val result = super.removeExceptionRequest(exceptionName)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.className == exceptionName
    )

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the exception request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeExceptionRequestWithId(
    requestId: String
  ): Boolean = {
    val result = super.removeExceptionRequestWithId(requestId)

    val pendingResult = pendingActionManager.removePendingActionsWithId(
      requestId
    )

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}
