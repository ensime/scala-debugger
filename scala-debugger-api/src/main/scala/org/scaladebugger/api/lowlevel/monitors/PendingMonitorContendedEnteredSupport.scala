package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending monitor contended entered capabilities to an existing
 * monitor contended entered manager.
 */
trait PendingMonitorContendedEnteredSupport extends PendingMonitorContendedEnteredSupportLike {
  /**
   * Represents the manager used to store pending monitor contended entered requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[MonitorContendedEnteredRequestInfo]

  /**
   * Processes all pending monitor contended entered requests.
   *
   * @return The collection of successfully-processed monitor contended entered requests
   */
  override def processAllPendingMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending monitor contended entered requests.
   *
   * @return The collection of monitor contended entered requests
   */
  override def pendingMonitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new monitor contended entered request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorContendedEnteredRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMonitorContendedEnteredRequest() = super.createMonitorContendedEnteredRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createMonitorContendedEnteredRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MonitorContendedEnteredRequestInfo(requestId, isPending = true, extraArguments),
          () => createMonitorContendedEnteredRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified monitor contended entered request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the monitor contended entered request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMonitorContendedEnteredRequest(id: String): Boolean = {
    val result = super.removeMonitorContendedEnteredRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real monitor contended entered request or any
    // pending monitor contended entered request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

