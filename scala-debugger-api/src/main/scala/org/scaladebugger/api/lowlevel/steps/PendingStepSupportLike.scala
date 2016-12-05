package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending step capabilities to an existing access
 * watchpoint manager.
 */
trait PendingStepSupportLike
  extends StepManager
  with PendingRequestSupport
{
  /**
   * Processes all pending step requests.
   *
   * @return The collection of successfully-processed step requests
   */
  def processAllPendingStepRequests(): Seq[StepRequestInfo]

  /**
   * Retrieves a list of all pending step requests.
   *
   * @return The collection of step request information
   */
  def pendingStepRequests: Seq[StepRequestInfo]

  /**
   * Processes all pending step requests for the specified thread.
   *
   * @param threadReference The thread whose pending requests to process
   *
   * @return The collection of successfully-processed step requests
   */
  def processPendingStepRequestsForThread(
    threadReference: ThreadReference
  ): Seq[StepRequestInfo]

  /**
   * Retrieves a list of pending step requests for the specified thread.
   *
   * @param threadReference The thread whose pending requests to retrieve
   *
   * @return The collection of successfully-processed step requests
   */
  def pendingStepRequestsForThread(
    threadReference: ThreadReference
  ): Seq[StepRequestInfo]
}
