package org.scaladebugger.api.lowlevel.classes

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending class prepare capabilities to an existing
 * class prepare manager.
 */
trait PendingClassPrepareSupportLike
  extends ClassPrepareManager
  with PendingRequestSupport
{
  /**
   * Processes all pending class prepare requests.
   *
   * @return The collection of successfully-processed class prepare requests
   */
  def processAllPendingClassPrepareRequests(): Seq[ClassPrepareRequestInfo]

  /**
   * Retrieves a list of pending class prepare requests.
   *
   * @return The collection of class prepare requests
   */
  def pendingClassPrepareRequests: Seq[ClassPrepareRequestInfo]
}

