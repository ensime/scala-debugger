package org.senkbeil.debugger.api.jdi.requests.properties.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.api.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.senkbeil.debugger.api.jdi.requests.properties.SuspendPolicyProperty

/**
 * Represents a processor for the suspend policy of a request.
 *
 * @param suspendPolicy The suspend policy property to use when processing
 */
class SuspendPolicyPropertyProcessor(
  val suspendPolicy: SuspendPolicyProperty
) extends JDIRequestProcessor {
  private val policy = suspendPolicy.policy

  /**
   * Processes the provided event request with the property logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    eventRequest.setSuspendPolicy(policy)

    eventRequest
  }

  override val argument: JDIRequestArgument = suspendPolicy
}
