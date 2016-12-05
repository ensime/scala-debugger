package org.scaladebugger.api.lowlevel.breakpoints

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending breakpoint capabilities to an existing breakpoint manager.
 */
trait PendingBreakpointSupportLike
  extends BreakpointManager
  with PendingRequestSupport
{
  /**
   * Processes all pending breakpoint requests.
   *
   * @return The collection of successfully-processed breakpoint requests
   */
  def processAllPendingBreakpointRequests(): Seq[BreakpointRequestInfo]

  /**
   * Retrieves a list of all pending breakpoint requests.
   *
   * @return The collection of breakpoint request information
   */
  def pendingBreakpointRequests: Seq[BreakpointRequestInfo]

  /**
   * Processes all pending breakpoint requests for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoint requests to
   *                 process
   *
   * @return The collection of successfully-processed breakpoint requests
   */
  def processPendingBreakpointRequestsForFile(
    fileName: String
  ): Seq[BreakpointRequestInfo]

  /**
   * Retrieves a list of pending breakpoint requests for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoint requests to
   *                 retrieve
   *
   * @return The collection of breakpoint request information
   */
  def pendingBreakpointRequestsForFile(
    fileName: String
  ): Seq[BreakpointRequestInfo]
}
