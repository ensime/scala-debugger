package org.scaladebugger.api.lowlevel.threads

import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a thread death request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param extraArguments The additional arguments provided to the
 *                       thread death request
 */
case class ThreadDeathRequestInfo(
  requestId: String,
  isPending: Boolean,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

