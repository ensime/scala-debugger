package org.scaladebugger.api.lowlevel.vm

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending vm death capabilities to an existing
 * vm death manager.
 */
trait PendingVMDeathSupportLike extends VMDeathManager with PendingRequestSupport {
  /**
   * Processes all pending vm death requests.
   *
   * @return The collection of successfully-processed vm death requests
   */
  def processAllPendingVMDeathRequests(): Seq[VMDeathRequestInfo]

  /**
   * Retrieves a list of pending vm death requests.
   *
   * @return The collection of vm death requests
   */
  def pendingVMDeathRequests: Seq[VMDeathRequestInfo]
}

