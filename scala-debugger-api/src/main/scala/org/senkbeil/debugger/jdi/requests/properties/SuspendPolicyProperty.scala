package org.senkbeil.debugger.jdi.requests.properties

import com.sun.jdi.request.EventRequest
import org.senkbeil.debugger.jdi.requests.JDIRequestProcessor
import org.senkbeil.debugger.jdi.requests.properties.processors.SuspendPolicyPropertyProcessor

/**
 * Represents an argument used set the suspend policy of the request.
 *
 * @param policy The policy to use for the request
 */
case class SuspendPolicyProperty(policy: Int) extends JDIRequestProperty {
  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestProcessor =
    new SuspendPolicyPropertyProcessor(this)
}

/**
 * Contains singleton instances of the suspend policy property.
 */
object SuspendPolicyProperty {
  /** Represents a policy where all threads are suspended. */
  val AllThreads  = SuspendPolicyProperty(EventRequest.SUSPEND_ALL)

  /** Represents a policy where only the event's thread is suspended. */
  val EventThread = SuspendPolicyProperty(EventRequest.SUSPEND_EVENT_THREAD)

  /** Represents a policy where no thread is suspended. */
  val NoThread    = SuspendPolicyProperty(EventRequest.SUSPEND_NONE)
}
