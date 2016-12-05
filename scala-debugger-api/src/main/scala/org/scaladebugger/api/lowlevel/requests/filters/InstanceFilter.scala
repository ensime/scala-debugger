package org.scaladebugger.api.lowlevel.requests.filters

import com.sun.jdi.ObjectReference
import org.scaladebugger.api.lowlevel.requests.JDIRequestProcessor
import org.scaladebugger.api.lowlevel.requests.filters.processors.InstanceFilterProcessor

/**
 * Represents a filter used to limit requests to a specific instance of a class.
 *
 * @note Only used by AccessWatchpointRequest, BreakpointRequest,
 *       ExceptionRequest, MethodEntryRequest, MethodExitRequest,
 *       ModificationWatchpointRequest, MonitorContendedEnteredRequest,
 *       MonitorContendedEnterRequest, MonitorWaitedRequest, MonitorWaitRequest,
 *       and StepRequest.
 *
 * @param objectReference The object reference used to specify the instance
 */
case class InstanceFilter(
  objectReference: ObjectReference
) extends JDIRequestFilter {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestFilterProcessor =
    new InstanceFilterProcessor(this)
}
