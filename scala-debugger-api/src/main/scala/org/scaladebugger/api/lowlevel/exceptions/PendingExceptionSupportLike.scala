package org.scaladebugger.api.lowlevel.exceptions

import org.scaladebugger.api.lowlevel.PendingRequestSupport

/**
 * Provides pending exception capabilities to an existing exception manager.
 */
trait PendingExceptionSupportLike
  extends ExceptionManager
  with PendingRequestSupport
{
  /**
   * Processes all pending exception requests.
   *
   * @return The collection of successfully-processed exception requests
   */
  def processAllPendingExceptionRequests(): Seq[ExceptionRequestInfo]

  /**
   * Retrieves a list of all pending exception requests.
   *
   * @return The collection of exception request information
   */
  def pendingExceptionRequests: Seq[ExceptionRequestInfo]

  /**
   * Processes all pending exception requests for the specified file.
   *
   * @param className The full name of the exception class whose pending
   *                  exception requests to process
   *
   * @return The collection of successfully-processed exception requests
   */
  def processPendingExceptionRequestsForClass(
    className: String
  ): Seq[ExceptionRequestInfo]

  /**
   * Retrieves a list of pending exception requests for the specified file.
   *
   * @param className The full name of the exception class whose pending
   *                  exception requests to retrieve
   *
   * @return The collection of successfully-processed exception requests
   */
  def pendingExceptionRequestsForClass(
    className: String
  ): Seq[ExceptionRequestInfo]
}
