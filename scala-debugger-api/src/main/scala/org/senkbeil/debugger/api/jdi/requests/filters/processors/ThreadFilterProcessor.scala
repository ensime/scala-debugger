package org.senkbeil.debugger.api.jdi.requests.filters.processors

import com.sun.jdi.ThreadReference
import com.sun.jdi.request._
import org.senkbeil.debugger.api.jdi.requests.{JDIRequestProcessor, JDIRequestArgument}
import org.senkbeil.debugger.api.jdi.requests.filters.ThreadFilter

/**
 * Represents a processor for the thread filter.
 *
 * @param threadFilter The thread filter to use when processing
 */
class ThreadFilterProcessor(
  val threadFilter: ThreadFilter
) extends JDIRequestProcessor {
  private val threadReference = threadFilter.threadReference

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
      case r: AccessWatchpointRequest         => r.addThreadFilter _
      case r: BreakpointRequest               => r.addThreadFilter _
      case r: ExceptionRequest                => r.addThreadFilter _
      case r: MethodEntryRequest              => r.addThreadFilter _
      case r: MethodExitRequest               => r.addThreadFilter _
      case r: ModificationWatchpointRequest   => r.addThreadFilter _
      case r: MonitorContendedEnteredRequest  => r.addThreadFilter _
      case r: MonitorContendedEnterRequest    => r.addThreadFilter _
      case r: MonitorWaitedRequest            => r.addThreadFilter _
      case r: MonitorWaitRequest              => r.addThreadFilter _
      case r: ThreadDeathRequest              => r.addThreadFilter _
      case r: ThreadStartRequest              => r.addThreadFilter _
      case _                                  => (_: ThreadReference) => {}
    })(threadReference)

    eventRequest
  }

  override val argument: JDIRequestArgument = threadFilter
}
