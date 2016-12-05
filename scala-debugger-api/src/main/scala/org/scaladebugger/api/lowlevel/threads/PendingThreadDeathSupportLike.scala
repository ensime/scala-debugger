package org.scaladebugger.api.lowlevel.threads

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending thread death capabilities to an existing
 * thread death manager.
 */
trait PendingThreadDeathSupportLike
  extends ThreadDeathManager
  with PendingRequestSupport
{
  /**
   * Processes all pending thread death requests.
   *
   * @return The collection of successfully-processed thread death requests
   */
  def processAllPendingThreadDeathRequests(): Seq[ThreadDeathRequestInfo]

  /**
   * Retrieves a list of pending thread death requests.
   *
   * @return The collection of thread death requests
   */
  def pendingThreadDeathRequests: Seq[ThreadDeathRequestInfo]
}

