package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending monitor contended enter capabilities to an existing
 * monitor contended enter manager.
 */
trait PendingMonitorContendedEnterSupportLike
  extends MonitorContendedEnterManager
  with PendingRequestSupport
{
  /**
   * Processes all pending monitor contended enter requests.
   *
   * @return The collection of successfully-processed monitor contended enter requests
   */
  def processAllPendingMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo]

  /**
   * Retrieves a list of pending monitor contended enter requests.
   *
   * @return The collection of monitor contended enter requests
   */
  def pendingMonitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo]
}

