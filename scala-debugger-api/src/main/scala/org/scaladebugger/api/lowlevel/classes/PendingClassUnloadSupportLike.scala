package org.scaladebugger.api.lowlevel.classes

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending class unload capabilities to an existing
 * class unload manager.
 */
trait PendingClassUnloadSupportLike
  extends ClassUnloadManager
  with PendingRequestSupport
{
  /**
   * Processes all pending class unload requests.
   *
   * @return The collection of successfully-processed class unload requests
   */
  def processAllPendingClassUnloadRequests(): Seq[ClassUnloadRequestInfo]

  /**
   * Retrieves a list of pending class unload requests.
   *
   * @return The collection of class unload requests
   */
  def pendingClassUnloadRequests: Seq[ClassUnloadRequestInfo]
}

