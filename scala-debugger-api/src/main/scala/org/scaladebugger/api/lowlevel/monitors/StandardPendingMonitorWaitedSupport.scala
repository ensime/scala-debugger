package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending monitor waited capabilities to an existing
 * monitor waited manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMonitorWaitedSupport extends PendingMonitorWaitedSupport {
  override protected val pendingActionManager: PendingActionManager[MonitorWaitedRequestInfo] =
    new PendingActionManager[MonitorWaitedRequestInfo]
}
