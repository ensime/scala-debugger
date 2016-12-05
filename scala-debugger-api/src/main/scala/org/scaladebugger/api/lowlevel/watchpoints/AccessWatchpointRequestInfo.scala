package org.scaladebugger.api.lowlevel.watchpoints

import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a access watchpoint request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param className The full name of the class whose field to watch
 * @param fieldName The name of the field to watch
 * @param extraArguments The additional arguments provided to the
 *                       access watchpoint request
 */
case class AccessWatchpointRequestInfo(
  requestId: String,
  isPending: Boolean,
  className: String,
  fieldName: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

