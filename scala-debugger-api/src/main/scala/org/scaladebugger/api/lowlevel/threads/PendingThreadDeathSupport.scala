package org.scaladebugger.api.lowlevel.threads

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending thread death capabilities to an existing
 * thread death manager.
 */
trait PendingThreadDeathSupport extends PendingThreadDeathSupportLike {
  /**
   * Represents the manager used to store pending thread death requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[ThreadDeathRequestInfo]

  /**
   * Processes all pending thread death requests.
   *
   * @return The collection of successfully-processed thread death requests
   */
  override def processAllPendingThreadDeathRequests(): Seq[ThreadDeathRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending thread death requests.
   *
   * @return The collection of thread death requests
   */
  override def pendingThreadDeathRequests: Seq[ThreadDeathRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new thread death request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createThreadDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createThreadDeathRequest() = super.createThreadDeathRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createThreadDeathRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ThreadDeathRequestInfo(requestId, isPending = true, extraArguments),
          () => createThreadDeathRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified thread death request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the thread death request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeThreadDeathRequest(id: String): Boolean = {
    val result = super.removeThreadDeathRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real thread death request or any
    // pending thread death request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

