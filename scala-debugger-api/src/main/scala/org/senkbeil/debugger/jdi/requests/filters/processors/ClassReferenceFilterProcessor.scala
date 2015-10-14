package org.senkbeil.debugger.jdi.requests.filters.processors

import com.sun.jdi.ReferenceType
import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.filters.ClassReferenceFilter
import org.senkbeil.debugger.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}

/**
 * Represents a processor for the class reference filter.
 *
 * @param classReferenceFilter The class reference filter to use when processing
 */
class ClassReferenceFilterProcessor(
  val classReferenceFilter: ClassReferenceFilter
) extends JDIRequestProcessor {
  private val referenceType = classReferenceFilter.referenceType

  /**
   * Processes the provided event request with the filter logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    type RT = ReferenceType
    // Apply the filter to the JDI request if it supports the filter
    if (eventRequest != null) (eventRequest match {
      case r: AccessWatchpointRequest         => r.addClassFilter(_: RT)
      case r: ClassPrepareRequest             => r.addClassFilter(_: RT)
      case r: ExceptionRequest                => r.addClassFilter(_: RT)
      case r: MethodEntryRequest              => r.addClassFilter(_: RT)
      case r: MethodExitRequest               => r.addClassFilter(_: RT)
      case r: ModificationWatchpointRequest   => r.addClassFilter(_: RT)
      case r: MonitorContendedEnteredRequest  => r.addClassFilter(_: RT)
      case r: MonitorContendedEnterRequest    => r.addClassFilter(_: RT)
      case r: MonitorWaitedRequest            => r.addClassFilter(_: RT)
      case r: MonitorWaitRequest              => r.addClassFilter(_: RT)
      case r: StepRequest                     => r.addClassFilter(_: RT)
      case _                                  => (_: RT) => {}
    })(referenceType)

    eventRequest
  }

  override val argument: JDIRequestArgument = classReferenceFilter
}
