package org.scaladebugger.api.lowlevel.methods

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending method exit capabilities to an existing access
 * watchpoint manager.
 */
trait PendingMethodExitSupport extends PendingMethodExitSupportLike {
  /**
   * Represents the manager used to store pending method exit requests
   * and process them later.
   */
  protected val pendingActionManager: PendingActionManager[MethodExitRequestInfo]

  /**
   * Processes all pending method exit requests.
   *
   * @return The collection of successfully-processed method exit requests
   */
  override def processAllPendingMethodExitRequests(): Seq[MethodExitRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending method exit requests.
   *
   * @return The collection of method exit request information
   */
  override def pendingMethodExitRequests: Seq[MethodExitRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending method exit requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  method exit requests to process
   *
   * @return The collection of successfully-processed method exit requests
   */
  override def processPendingMethodExitRequestsForClass(
    className: String
  ): Seq[MethodExitRequestInfo] = {
    pendingActionManager.processActions(_.data.className == className)
      .map(_.data)
  }

  /**
   * Retrieves a list of pending method exit requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  method exit requests to retrieve
   *
   * @return The collection of successfully-processed method exit requests
   */
  override def pendingMethodExitRequestsForClass(
    className: String
  ): Seq[MethodExitRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.className == className)
  }

  /**
   * Creates a new method exit request for the specified method using the
   * method's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the method
   * @param methodName The name of the method to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMethodExitRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMethodExit() = super.createMethodExitRequestWithId(
      requestId,
      className,
      methodName,
      extraArguments: _*
    )

    val result = createMethodExit()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MethodExitRequestInfo(
            requestId,
            isPending = true,
            className,
            methodName,
            extraArguments
          ),
          () => createMethodExit().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the method exit for the specified method.
   *
   * @param className The name of the class containing the method
   * @param methodName The name of the method to watch
   *
   * @return True if successfully removed method exit, otherwise false
   */
  abstract override def removeMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    val result = super.removeMethodExitRequest(className, methodName)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.className == className && a.data.methodName == methodName
    )

    // True if we removed a real request or any pending request
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the method exit request with the specified id.
   *
   * @param id The id of the Method Exit Request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMethodExitRequestWithId(
    id: String
  ): Boolean = {
    val result = super.removeMethodExitRequestWithId(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}
