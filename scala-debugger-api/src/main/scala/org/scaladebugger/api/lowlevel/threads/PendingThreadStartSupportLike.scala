package org.scaladebugger.api.lowlevel.threads

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending thread start capabilities to an existing
 * thread start manager.
 */
trait PendingThreadStartSupportLike
  extends ThreadStartManager
  with PendingRequestSupport
{
  /**
   * Processes all pending thread start requests.
   *
   * @return The collection of successfully-processed thread start requests
   */
  def processAllPendingThreadStartRequests(): Seq[ThreadStartRequestInfo]

  /**
   * Retrieves a list of pending thread start requests.
   *
   * @return The collection of thread start requests
   */
  def pendingThreadStartRequests: Seq[ThreadStartRequestInfo]
}

