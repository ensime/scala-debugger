package org.senkbeil.debugger.jdi.requests.filters.processors

import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.filters.SourceNameFilter
import org.senkbeil.debugger.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}

/**
 * Represents a processor for the source name filter.
 *
 * @param sourceNameFilter The source name filter to use when processing
 */
class SourceNameFilterProcessor(
  val sourceNameFilter: SourceNameFilter
) extends JDIRequestProcessor {
  private val sourceNamePattern = sourceNameFilter.sourceNamePattern

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
      case r: ClassPrepareRequest => r.addSourceNameFilter _
      case _                      => (_: String) => {}
    })(sourceNamePattern)

    eventRequest
  }

  override val argument: JDIRequestArgument = sourceNameFilter
}
