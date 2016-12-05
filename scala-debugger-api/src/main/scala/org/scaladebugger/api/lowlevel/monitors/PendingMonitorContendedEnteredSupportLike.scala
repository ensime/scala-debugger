package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending monitor contended entered capabilities to an existing
 * monitor contended entered manager.
 */
trait PendingMonitorContendedEnteredSupportLike
  extends MonitorContendedEnteredManager
  with PendingRequestSupport
{
  /**
   * Processes all pending monitor contended entered requests.
   *
   * @return The collection of successfully-processed monitor contended entered requests
   */
  def processAllPendingMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo]

  /**
   * Retrieves a list of pending monitor contended entered requests.
   *
   * @return The collection of monitor contended entered requests
   */
  def pendingMonitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo]
}

