package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending modification watchpoint capabilities to an
 * existing modification watchpoint manager.
 */
trait PendingModificationWatchpointSupportLike
  extends ModificationWatchpointManager
  with PendingRequestSupport
{
  /**
   * Processes all pending modification watchpoint requests.
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  def processAllPendingModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo]

  /**
   * Retrieves a list of all pending modification watchpoint requests.
   *
   * @return The collection of modification watchpoint request information
   */
  def pendingModificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo]

  /**
   * Processes all pending modification watchpoint requests for the specified
   * class.
   *
   * @param className The full name of the class whose pending
   *                  modification watchpoint requests to process
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  def processPendingModificationWatchpointRequestsForClass(
    className: String
  ): Seq[ModificationWatchpointRequestInfo]

  /**
   * Retrieves a list of pending modification watchpoint requests for the
   * specified class.
   *
   * @param className The full name of the class whose pending
   *                  modification watchpoint requests to retrieve
   *
   * @return The collection of successfully-processed modification
   *         watchpoint requests
   */
  def pendingModificationWatchpointRequestsForClass(
    className: String
  ): Seq[ModificationWatchpointRequestInfo]
}
