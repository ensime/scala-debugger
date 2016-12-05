package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending access watchpoint capabilities to an existing access
 * watchpoint manager.
 */
trait PendingAccessWatchpointSupportLike
  extends AccessWatchpointManager
  with PendingRequestSupport
{
  /**
   * Processes all pending access watchpoint requests.
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  def processAllPendingAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo]

  /**
   * Retrieves a list of all pending access watchpoint requests.
   *
   * @return The collection of access watchpoint request information
   */
  def pendingAccessWatchpointRequests: Seq[AccessWatchpointRequestInfo]

  /**
   * Processes all pending access watchpoint requests for the specified class.
   *
   * @param className The full name of the class whose pending
   *                  access watchpoint requests to process
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  def processPendingAccessWatchpointRequestsForClass(
    className: String
  ): Seq[AccessWatchpointRequestInfo]

  /**
   * Retrieves a list of pending access watchpoint requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  access watchpoint requests to retrieve
   *
   * @return The collection of successfully-processed access watchpoint requests
   */
  def pendingAccessWatchpointRequestsForClass(
    className: String
  ): Seq[AccessWatchpointRequestInfo]
}
