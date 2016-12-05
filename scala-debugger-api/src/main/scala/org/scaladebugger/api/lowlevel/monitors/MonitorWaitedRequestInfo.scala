package org.scaladebugger.api.lowlevel.monitors

import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a monitor waited request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param extraArguments The additional arguments provided to the
 *                       monitor waited request
 */
case class MonitorWaitedRequestInfo(
  requestId: String,
  isPending: Boolean,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

