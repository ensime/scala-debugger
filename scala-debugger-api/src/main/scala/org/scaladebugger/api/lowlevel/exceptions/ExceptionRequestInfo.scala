package org.scaladebugger.api.lowlevel.exceptions

import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import ExceptionRequestInfo._

/**
 * Represents information about an exception request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param className The full name of the exception class
 * @param notifyCaught Whether or not caught exceptions are reported
 * @param notifyUncaught Whether or not uncaught exceptions are reported
 * @param extraArguments The additional arguments provided to the exception
 */
case class ExceptionRequestInfo(
  requestId: String,
  isPending: Boolean,
  className: String,
  notifyCaught: Boolean,
  notifyUncaught: Boolean,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo {
  /**
   * Indicates whether or not this exception request was to report all
   * exceptions or a specific exception.
   *
   * @return True if all exceptions are intended to be reported with this
   *         request, otherwise false
   */
  def isCatchall: Boolean = className == DefaultCatchallExceptionName
}

object ExceptionRequestInfo {
  /** Represents the standard name for an request to catch all exceptions. */
  val DefaultCatchallExceptionName = "<CATCHALL>"
}

