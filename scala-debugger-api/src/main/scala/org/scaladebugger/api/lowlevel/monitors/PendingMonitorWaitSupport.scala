package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending monitor wait capabilities to an existing
 * monitor wait manager.
 */
trait PendingMonitorWaitSupport extends PendingMonitorWaitSupportLike {
  /**
   * Represents the manager used to store pending monitor wait requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[MonitorWaitRequestInfo]

  /**
   * Processes all pending monitor wait requests.
   *
   * @return The collection of successfully-processed monitor wait requests
   */
  override def processAllPendingMonitorWaitRequests(): Seq[MonitorWaitRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending monitor wait requests.
   *
   * @return The collection of monitor wait requests
   */
  override def pendingMonitorWaitRequests: Seq[MonitorWaitRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new monitor wait request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorWaitRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMonitorWaitRequest() = super.createMonitorWaitRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createMonitorWaitRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MonitorWaitRequestInfo(requestId, isPending = true, extraArguments),
          () => createMonitorWaitRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified monitor wait request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the monitor wait request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMonitorWaitRequest(id: String): Boolean = {
    val result = super.removeMonitorWaitRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real monitor wait request or any
    // pending monitor wait request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

