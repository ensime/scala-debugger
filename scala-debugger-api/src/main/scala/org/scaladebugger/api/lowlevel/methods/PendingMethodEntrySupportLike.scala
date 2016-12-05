package org.scaladebugger.api.lowlevel.methods

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending method entry capabilities to an existing access
 * watchpoint manager.
 */
trait PendingMethodEntrySupportLike
  extends MethodEntryManager
  with PendingRequestSupport
{
  /**
   * Processes all pending method entry requests.
   *
   * @return The collection of successfully-processed method entry requests
   */
  def processAllPendingMethodEntryRequests(): Seq[MethodEntryRequestInfo]

  /**
   * Retrieves a list of all pending method entry requests.
   *
   * @return The collection of method entry request information
   */
  def pendingMethodEntryRequests: Seq[MethodEntryRequestInfo]

  /**
   * Processes all pending method entry requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  method entry requests to process
   *
   * @return The collection of successfully-processed method entry requests
   */
  def processPendingMethodEntryRequestsForClass(
    className: String
  ): Seq[MethodEntryRequestInfo]

  /**
   * Retrieves a list of pending method entry requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  method entry requests to retrieve
   *
   * @return The collection of successfully-processed method entry requests
   */
  def pendingMethodEntryRequestsForClass(
    className: String
  ): Seq[MethodEntryRequestInfo]
}
