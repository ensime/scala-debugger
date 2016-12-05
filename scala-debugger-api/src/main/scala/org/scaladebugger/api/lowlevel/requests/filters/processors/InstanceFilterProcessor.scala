package org.scaladebugger.api.lowlevel.requests.filters.processors

import com.sun.jdi.ObjectReference
import com.sun.jdi.request._
import org.scaladebugger.api.lowlevel.requests.filters.{JDIRequestFilter, JDIRequestFilterProcessor, InstanceFilter}

/**
 * Represents a processor for the instance filter.
 *
 * @param instanceFilter The instance filter to use when processing
 */
class InstanceFilterProcessor(
  val instanceFilter: InstanceFilter
) extends JDIRequestFilterProcessor {
  private val objectReference = instanceFilter.objectReference

  /**
   * Processes the provided event request with the filter logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    // Apply the filter to the JDI request if it supports the filter
    if (eventRequest != null) (eventRequest match {
      case r: AccessWatchpointRequest         => r.addInstanceFilter _
      case r: BreakpointRequest               => r.addInstanceFilter _
      case r: ExceptionRequest                => r.addInstanceFilter _
      case r: MethodEntryRequest              => r.addInstanceFilter _
      case r: MethodExitRequest               => r.addInstanceFilter _
      case r: ModificationWatchpointRequest   => r.addInstanceFilter _
      case r: MonitorContendedEnteredRequest  => r.addInstanceFilter _
      case r: MonitorContendedEnterRequest    => r.addInstanceFilter _
      case r: MonitorWaitedRequest            => r.addInstanceFilter _
      case r: MonitorWaitRequest              => r.addInstanceFilter _
      case r: StepRequest                     => r.addInstanceFilter _
      case _                                  => (_: ObjectReference) => {}
    })(objectReference)

    eventRequest
  }

  override val argument: JDIRequestFilter = instanceFilter
}
