package org.scaladebugger.api.lowlevel.classes

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending class prepare capabilities to an existing
 * class prepare manager.
 */
trait PendingClassPrepareSupport extends PendingClassPrepareSupportLike {
  /**
   * Represents the manager used to store pending class prepare requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[ClassPrepareRequestInfo]

  /**
   * Processes all pending class prepare requests.
   *
   * @return The collection of successfully-processed class prepare requests
   */
  override def processAllPendingClassPrepareRequests(): Seq[ClassPrepareRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending class prepare requests.
   *
   * @return The collection of class prepare requests
   */
  override def pendingClassPrepareRequests: Seq[ClassPrepareRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new class prepare request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createClassPrepareRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createClassPrepareRequest() = super.createClassPrepareRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createClassPrepareRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ClassPrepareRequestInfo(requestId, isPending = true, extraArguments),
          () => createClassPrepareRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified class prepare request.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeClassPrepareRequest(id: String): Boolean = {
    val result = super.removeClassPrepareRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real class prepare request or any
    // pending class prepare request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

