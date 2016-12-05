package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Provides pending step capabilities to an existing access
 * watchpoint manager.
 */
trait PendingStepSupport extends PendingStepSupportLike {
  /**
   * Represents the manager used to store pending step requests
   * and process them later.
   */
  protected val pendingActionManager: PendingActionManager[StepRequestInfo]

  /**
   * Processes all pending step requests.
   *
   * @return The collection of successfully-processed step requests
   */
  override def processAllPendingStepRequests(): Seq[StepRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending step requests.
   *
   * @return The collection of step request information
   */
  override def pendingStepRequests: Seq[StepRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Processes all pending step requests for the specified thread.
   *
   * @param threadReference The thread whose pending requests to process
   *
   * @return The collection of successfully-processed step requests
   */
  override def processPendingStepRequestsForThread(
    threadReference: ThreadReference
  ): Seq[StepRequestInfo] = {
    pendingActionManager.processActions(
      _.data.threadReference == threadReference
    ).map(_.data)
  }

  /**
   * Retrieves a list of pending step requests for the specified thread.
   *
   * @param threadReference The thread whose pending requests to retrieve
   *
   * @return The collection of successfully-processed step requests
   */
  override def pendingStepRequestsForThread(
    threadReference: ThreadReference
  ): Seq[StepRequestInfo] = {
    pendingActionManager.getPendingActionData(
      _.data.threadReference == threadReference
    )
  }

  /**
   * Creates and enables a step request for the given thread using the provided
   * size (next valid location or next location on a new line) and depth (into,
   * over, or out of the current frame).
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param removeExistingRequests If true, will first remove any existing
   *                               step requests for the specified thread
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createStepRequestWithId(
    requestId: String,
    removeExistingRequests: Boolean,
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createStep() = super.createStepRequestWithId(
      requestId,
      removeExistingRequests,
      threadReference,
      size,
      depth,
      extraArguments: _*
    )

    val result = createStep()

    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          StepRequestInfo(
            requestId,
            isPending = true,
            removeExistingRequests,
            threadReference,
            size,
            depth,
            extraArguments
          ),
          () => createStep().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed the step request, otherwise false
   */
  abstract override def removeStepRequestWithId(
    requestId: String
  ): Boolean = {
    val result = super.removeStepRequestWithId(requestId)

    val pendingResult = pendingActionManager.removePendingActionsWithId(
      requestId
    )

    // True if we removed a real exception or any pending exceptions
    result || pendingResult.getOrElse(Nil).nonEmpty
  }

  /**
   * Removes the step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return True if successfully removed the step request, otherwise false
   */
  abstract override def removeStepRequest(
    threadReference: ThreadReference
  ): Boolean = {
    val result = super.removeStepRequest(threadReference)

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.threadReference == threadReference
    )

    // True if we removed a real request or any pending request
    result || pendingResult.nonEmpty
  }
}
