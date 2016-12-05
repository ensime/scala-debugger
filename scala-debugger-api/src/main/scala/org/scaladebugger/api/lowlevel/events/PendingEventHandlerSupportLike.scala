package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending event capabilities to an existing event manager. Note that
 * all newly-created event handlers will be pending while pending is enabled.
 */
trait PendingEventHandlerSupportLike
  extends EventManager
  with PendingRequestSupport
{
  /**
   * Processes all pending event handlers.
   *
   * @return The collection of successfully-processed event handlers
   */
  def processAllPendingEventHandlers(): Seq[EventHandlerInfo]

  /**
   * Retrieves a list of all pending event handlers.
   *
   * @return The collection of event request information
   */
  def pendingEventHandlers: Seq[EventHandlerInfo]
}
