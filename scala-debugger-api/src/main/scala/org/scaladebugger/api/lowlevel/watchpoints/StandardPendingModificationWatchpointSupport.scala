package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending modification watchpoint capabilities to an existing
 * modification watchpoint manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingModificationWatchpointSupport extends PendingModificationWatchpointSupport {
  override protected val pendingActionManager: PendingActionManager[ModificationWatchpointRequestInfo] =
    new PendingActionManager[ModificationWatchpointRequestInfo]
}
