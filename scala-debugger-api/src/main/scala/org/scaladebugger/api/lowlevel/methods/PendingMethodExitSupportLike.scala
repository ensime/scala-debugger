package org.scaladebugger.api.lowlevel.methods

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending method exit capabilities to an existing access
 * watchpoint manager.
 */
trait PendingMethodExitSupportLike
  extends MethodExitManager
  with PendingRequestSupport
{
  /**
   * Processes all pending method exit requests.
   *
   * @return The collection of successfully-processed method exit requests
   */
  def processAllPendingMethodExitRequests(): Seq[MethodExitRequestInfo]

  /**
   * Retrieves a list of all pending method exit requests.
   *
   * @return The collection of method exit request information
   */
  def pendingMethodExitRequests: Seq[MethodExitRequestInfo]

  /**
   * Processes all pending method exit requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  method exit requests to process
   *
   * @return The collection of successfully-processed method exit requests
   */
  def processPendingMethodExitRequestsForClass(
    className: String
  ): Seq[MethodExitRequestInfo]

  /**
   * Retrieves a list of pending method exit requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  method exit requests to retrieve
   *
   * @return The collection of successfully-processed method exit requests
   */
  def pendingMethodExitRequestsForClass(
    className: String
  ): Seq[MethodExitRequestInfo]
}
