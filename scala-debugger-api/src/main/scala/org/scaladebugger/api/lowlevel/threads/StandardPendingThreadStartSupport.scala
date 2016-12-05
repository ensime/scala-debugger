package org.scaladebugger.api.lowlevel.threads

import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending thread start capabilities to an existing thread start
 * manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingThreadStartSupport extends PendingThreadStartSupport {
  override protected val pendingActionManager: PendingActionManager[ThreadStartRequestInfo] =
    new PendingActionManager[ThreadStartRequestInfo]
}
