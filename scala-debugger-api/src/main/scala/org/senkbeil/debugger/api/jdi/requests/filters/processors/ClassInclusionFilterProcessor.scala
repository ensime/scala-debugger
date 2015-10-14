package org.senkbeil.debugger.api.jdi.requests.filters.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.api.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.senkbeil.debugger.api.jdi.requests.filters.ClassInclusionFilter

/**
 * Represents a processor for the class exclusion filter.
 *
 * @param classInclusionFilter The class exclusion filter to use when processing
 */
class ClassInclusionFilterProcessor(
  val classInclusionFilter: ClassInclusionFilter
) extends JDIRequestProcessor {
  private val classPattern = classInclusionFilter.classPattern

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
      case r: AccessWatchpointRequest         => r.addClassFilter(_: String)
      case r: ClassPrepareRequest             => r.addClassFilter(_: String)
      case r: ClassUnloadRequest              => r.addClassFilter(_: String)
      case r: ExceptionRequest                => r.addClassFilter(_: String)
      case r: MethodEntryRequest              => r.addClassFilter(_: String)
      case r: MethodExitRequest               => r.addClassFilter(_: String)
      case r: ModificationWatchpointRequest   => r.addClassFilter(_: String)
      case r: MonitorContendedEnteredRequest  => r.addClassFilter(_: String)
      case r: MonitorContendedEnterRequest    => r.addClassFilter(_: String)
      case r: MonitorWaitedRequest            => r.addClassFilter(_: String)
      case r: MonitorWaitRequest              => r.addClassFilter(_: String)
      case r: StepRequest                     => r.addClassFilter(_: String)
      case _                                  => (_: String) => {}
    })(classPattern)

    eventRequest
  }

  override val argument: JDIRequestArgument = classInclusionFilter
}
