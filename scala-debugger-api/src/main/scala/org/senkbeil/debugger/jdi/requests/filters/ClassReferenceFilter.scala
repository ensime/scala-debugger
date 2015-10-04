package org.senkbeil.debugger.jdi.requests.filters

import com.sun.jdi.ReferenceType
import org.senkbeil.debugger.jdi.requests.processors.{ClassReferenceProcessor, JDIRequestProcessor}

/**
 * Represents a filter used to limit requests to a specific class reference.
 * Requests are checked by verifying the class containing the current method
 * being invoked.
 *
 * @note Only used by AccessWatchpointRequest, ClassPrepareRequest,
 *       ExceptionRequest, MethodEntryRequest, MethodExitRequest,
 *       ModificationWatchpointRequest, MonitorContendedEnteredRequest,
 *       MonitorContendedEnterRequest, MonitorWaitedRequest,
 *       MonitorWaitRequest, and StepRequest.
 *
 * @param referenceType The reference type of the class to filter by
 */
case class ClassReferenceFilter(
  referenceType: ReferenceType
) extends JDIRequestFilter {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestProcessor =
    new ClassReferenceProcessor(this)
}
