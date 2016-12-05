package org.scaladebugger.api.lowlevel.breakpoints

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending breakpoint capabilities to an existing breakpoint manager.
 */
trait PendingBreakpointSupport extends PendingBreakpointSupportLike {
  /**
   * Represents the manager used to store pending breakpoint requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[BreakpointRequestInfo]

  /**
   * Processes all pending breakpoint requests.
   *
   * @return The collection of successfully-processed breakpoint requests
   */
  override def processAllPendingBreakpointRequests(): Seq[BreakpointRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending breakpoint requests.
   *
   * @return The collection of breakpoint request information
   */
  override def pendingBreakpointRequests: Seq[BreakpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending breakpoint requests for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoint requests to
   *                 process
   *
   * @return The collection of successfully-processed breakpoint requests
   */
  override def processPendingBreakpointRequestsForFile(
    fileName: String
  ): Seq[BreakpointRequestInfo] = {
    pendingActionManager.processActions(_.data.fileName == fileName).map(_.data)
  }

  /**
   * Retrieves a list of pending breakpoint requests for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoint requests to
   *                 retrieve
   *
   * @return The collection of breakpoint request information
   */
  override def pendingBreakpointRequestsForFile(
    fileName: String
  ): Seq[BreakpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.fileName == fileName)
  }

  /**
   * Removes the breakpoint on the specified line of the file. Will also remove
   * any pending breakpoints.
   *
   * @param fileName The name of the file to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  abstract override def removeBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = {
    val result = super.removeBreakpointRequest(
      fileName,
      lineNumber
    )

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.fileName == fileName && a.data.lineNumber == lineNumber
    )

    // True if we removed a real breakpoint or any pending breakpoints
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the breakpoint on the specified line of the file. Will also remove
   * any pending breakpoints.
   *
   * @param requestId The id of the request to remove
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  abstract override def removeBreakpointRequestWithId(
    requestId: String
  ): Boolean = {
    val result = super.removeBreakpointRequestWithId(requestId)

    val pendingResult = pendingActionManager.removePendingActionsWithId(
      requestId
    )

    // True if we removed a real breakpoint or any pending breakpoints
    result || pendingResult.getOrElse(Nil).nonEmpty
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class. Will
   * also remove any pending breakpoints.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful or pending, otherwise Failure
   */
  abstract override def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createBreakpoint() = super.createBreakpointRequestWithId(
      requestId,
      fileName,
      lineNumber,
      extraArguments: _*
    )

    val result = createBreakpoint()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          BreakpointRequestInfo(
            requestId,
            isPending = true,
            fileName,
            lineNumber,
            extraArguments
          ),
          () => createBreakpoint().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }
}
