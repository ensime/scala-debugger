package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.lowlevel.PendingRequestSupport
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending access watchpoint capabilities to an existing access
 * watchpoint manager.
 */
trait PendingAccessWatchpointSupport extends PendingAccessWatchpointSupportLike {
  /**
   * Represents the manager used to store pending access watchpoint requests
   * and process them later.
   */
  protected val pendingActionManager: PendingActionManager[AccessWatchpointRequestInfo]

  /**
   * Processes all pending access watchpoint requests.
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  override def processAllPendingAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending access watchpoint requests.
   *
   * @return The collection of access watchpoint request information
   */
  override def pendingAccessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending access watchpoint requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  access watchpoint requests to process
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  override def processPendingAccessWatchpointRequestsForClass(
    className: String
  ): Seq[AccessWatchpointRequestInfo] = {
    pendingActionManager.processActions(_.data.className == className)
      .map(_.data)
  }

  /**
   * Retrieves a list of pending access watchpoint requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  access watchpoint requests to retrieve
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  override def pendingAccessWatchpointRequestsForClass(
    className: String
  ): Seq[AccessWatchpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.className == className)
  }

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createAccessWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createAccessWatchpoint() = super.createAccessWatchpointRequestWithId(
      requestId,
      className,
      fieldName,
      extraArguments: _*
    )

    val result = createAccessWatchpoint()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          AccessWatchpointRequestInfo(
            requestId,
            isPending = true,
            className,
            fieldName,
            extraArguments
          ),
          () => createAccessWatchpoint().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if successfully removed access watchpoint, otherwise false
   */
  abstract override def removeAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = {
    val result = super.removeAccessWatchpointRequest(className, fieldName)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.className == className && a.data.fieldName == fieldName
    )

    // True if we removed a real request or any pending request
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   *
   * @return True if the access watchpoint request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeAccessWatchpointRequestWithId(
    id: String
  ): Boolean = {
    val result = super.removeAccessWatchpointRequestWithId(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}
