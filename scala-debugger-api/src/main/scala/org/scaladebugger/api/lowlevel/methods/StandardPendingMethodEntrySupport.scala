package org.scaladebugger.api.lowlevel.methods

import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending method entry capabilities to an existing method entry
 * manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMethodEntrySupport extends PendingMethodEntrySupport {
  override protected val pendingActionManager: PendingActionManager[MethodEntryRequestInfo] =
    new PendingActionManager[MethodEntryRequestInfo]
}
