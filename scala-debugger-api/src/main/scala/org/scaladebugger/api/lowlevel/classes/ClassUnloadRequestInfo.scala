package org.scaladebugger.api.lowlevel.classes

import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a class unload request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param extraArguments The additional arguments provided to the
 *                       class unload request
 */
case class ClassUnloadRequestInfo(
  requestId: String,
  isPending: Boolean,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

