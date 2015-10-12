package org.senkbeil.debugger.jdi.requests.properties.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.properties.SuspendPolicyProperty
import org.senkbeil.debugger.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}

/**
 * Represents a processor for the suspend policy of a request.
 *
 * @param suspendPolicy The suspend policy property to use when processing
 */
class SuspendPolicyProcessor(
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
