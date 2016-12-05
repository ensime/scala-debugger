package org.scaladebugger.api.lowlevel.classes

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending class unload capabilities to an existing
 * class unload manager.
 */
trait PendingClassUnloadSupport extends PendingClassUnloadSupportLike {
  /**
   * Represents the manager used to store pending class unload requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[ClassUnloadRequestInfo]

  /**
   * Processes all pending class unload requests.
   *
   * @return The collection of successfully-processed class unload requests
   */
  override def processAllPendingClassUnloadRequests(): Seq[ClassUnloadRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending class unload requests.
   *
   * @return The collection of class unload requests
   */
  override def pendingClassUnloadRequests: Seq[ClassUnloadRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new class unload request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createClassUnloadRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createClassUnloadRequest() = super.createClassUnloadRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createClassUnloadRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ClassUnloadRequestInfo(requestId, isPending = true, extraArguments),
          () => createClassUnloadRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified class unload request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the class unload request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeClassUnloadRequest(id: String): Boolean = {
    val result = super.removeClassUnloadRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real class unload request or any
    // pending class unload request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

