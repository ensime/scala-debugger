package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending monitor wait capabilities to an existing
 * monitor wait manager.
 */
trait PendingMonitorWaitSupportLike
  extends MonitorWaitManager
  with PendingRequestSupport
{
  /**
   * Processes all pending monitor wait requests.
   *
   * @return The collection of successfully-processed monitor wait requests
   */
  def processAllPendingMonitorWaitRequests(): Seq[MonitorWaitRequestInfo]

  /**
   * Retrieves a list of pending monitor wait requests.
   *
   * @return The collection of monitor wait requests
   */
  def pendingMonitorWaitRequests: Seq[MonitorWaitRequestInfo]
}

