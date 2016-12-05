package org.scaladebugger.api.lowlevel.methods

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending method entry capabilities to an existing access
 * watchpoint manager.
 */
trait PendingMethodEntrySupport extends PendingMethodEntrySupportLike {
  /**
   * Represents the manager used to store pending method entry requests
   * and process them later.
   */
  protected val pendingActionManager: PendingActionManager[MethodEntryRequestInfo]

  /**
   * Processes all pending method entry requests.
   *
   * @return The collection of successfully-processed method entry requests
   */
  override def processAllPendingMethodEntryRequests(): Seq[MethodEntryRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending method entry requests.
   *
   * @return The collection of method entry request information
   */
  override def pendingMethodEntryRequests: Seq[MethodEntryRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending method entry requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  method entry requests to process
   *
   * @return The collection of successfully-processed method entry requests
   */
  override def processPendingMethodEntryRequestsForClass(
    className: String
  ): Seq[MethodEntryRequestInfo] = {
    pendingActionManager.processActions(_.data.className == className)
      .map(_.data)
  }

  /**
   * Retrieves a list of pending method entry requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  method entry requests to retrieve
   *
   * @return The collection of successfully-processed method entry requests
   */
  override def pendingMethodEntryRequestsForClass(
    className: String
  ): Seq[MethodEntryRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.className == className)
  }

  /**
   * Creates a new method entry request for the specified method using the
   * method's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the method
   * @param methodName The name of the method to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMethodEntryRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMethodEntry() = super.createMethodEntryRequestWithId(
      requestId,
      className,
      methodName,
      extraArguments: _*
    )

    val result = createMethodEntry()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MethodEntryRequestInfo(
            requestId,
            isPending = true,
            className,
            methodName,
            extraArguments
          ),
          () => createMethodEntry().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the method entry for the specified method.
   *
   * @param className The name of the class containing the method
   * @param methodName The name of the method to watch
   *
   * @return True if successfully removed method entry, otherwise false
   */
  abstract override def removeMethodEntryRequest(
    className: String,
    methodName: String
  ): Boolean = {
    val result = super.removeMethodEntryRequest(className, methodName)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.className == className && a.data.methodName == methodName
    )

    // True if we removed a real request or any pending request
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the method entry request with the specified id.
   *
   * @param id The id of the Method Entry Request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMethodEntryRequestWithId(
    id: String
  ): Boolean = {
    val result = super.removeMethodEntryRequestWithId(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}
