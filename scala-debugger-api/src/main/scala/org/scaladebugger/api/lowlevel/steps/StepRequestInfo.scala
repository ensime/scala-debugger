package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.RequestInfo
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a step request.
 *
 * @param requestId The id of the request
 * @param isPending Whether or not this request is pending (not on remote JVM)
 * @param removeExistingRequests If true, will first remove any existing
 *                               step requests for the specified thread
 * @param threadReference The thread monitored for steps
 * @param size The size of the step (LINE/MIN)
 * @param depth The depth of the step (INTO/OVER/OUT)
 * @param extraArguments The additional arguments provided to the step request
 */
case class StepRequestInfo(
  requestId: String,
  isPending: Boolean,
  removeExistingRequests: Boolean,
  threadReference: ThreadReference,
  size: Int,
  depth: Int,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

