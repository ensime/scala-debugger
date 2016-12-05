package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending monitor waited capabilities to an existing
 * monitor waited manager.
 */
trait PendingMonitorWaitedSupportLike
  extends MonitorWaitedManager
  with PendingRequestSupport
{
  /**
   * Processes all pending monitor waited requests.
   *
   * @return The collection of successfully-processed monitor waited requests
   */
  def processAllPendingMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo]

  /**
   * Retrieves a list of pending monitor waited requests.
   *
   * @return The collection of monitor waited requests
   */
  def pendingMonitorWaitedRequests: Seq[MonitorWaitedRequestInfo]
}

