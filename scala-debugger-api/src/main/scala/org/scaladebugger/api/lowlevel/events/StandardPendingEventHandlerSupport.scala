package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending event capabilities to an existing event manager.
 * Contains an internal pending action manager.
 */
trait StandardPendingEventHandlerSupport extends PendingEventHandlerSupport {
  override protected val pendingActionManager: PendingActionManager[EventHandlerInfo] =
    new PendingActionManager[EventHandlerInfo]
}
