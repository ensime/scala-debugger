package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.lowlevel.PendingRequestSupport
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending modification watchpoint capabilities to an
 * existing modification watchpoint manager.
 */
trait PendingModificationWatchpointSupport extends PendingModificationWatchpointSupportLike {
  /**
   * Represents the manager used to store pending modification watchpoint
   * requests and process them later.
   */
  protected val pendingActionManager: PendingActionManager[ModificationWatchpointRequestInfo]

  /**
   * Processes all pending modification watchpoint requests.
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  override def processAllPendingModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending modification watchpoint requests.
   *
   * @return The collection of modification watchpoint request information
   */
  override def pendingModificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending modification watchpoint requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  modification watchpoint requests to process
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  override def processPendingModificationWatchpointRequestsForClass(
    className: String
  ): Seq[ModificationWatchpointRequestInfo] = {
    pendingActionManager.processActions(_.data.className == className)
      .map(_.data)
  }

  /**
   * Retrieves a list of pending modification watchpoint requests for the
   * specified class.
   *
   * @param className The full name of the class whose pending
   *                  modification watchpoint requests to retrieve
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  override def pendingModificationWatchpointRequestsForClass(
    className: String
  ): Seq[ModificationWatchpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.className == className)
  }

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createModificationWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createModificationWatchpoint() = super.createModificationWatchpointRequestWithId(
      requestId,
      className,
      fieldName,
      extraArguments: _*
    )

    val result = createModificationWatchpoint()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ModificationWatchpointRequestInfo(
            requestId,
            isPending = true,
            className,
            fieldName,
            extraArguments
          ),
          () => createModificationWatchpoint().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if successfully removed modification watchpoint,
   *         otherwise false
   */
  abstract override def removeModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = {
    val result = super.removeModificationWatchpointRequest(className, fieldName)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.className == className && a.data.fieldName == fieldName
    )

    // True if we removed a real request or any pending request
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   *
   * @return True if the modification watchpoint request was removed
   *         (if it existed), otherwise false
   */
  abstract override def removeModificationWatchpointRequestWithId(
    id: String
  ): Boolean = {
    val result = super.removeModificationWatchpointRequestWithId(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}
