package org.senkbeil.debugger.api.jdi.requests.filters.processors

import com.sun.jdi.request.EventRequest
import org.senkbeil.debugger.api.jdi.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.senkbeil.debugger.api.jdi.requests.filters.CountFilter

/**
 * Represents a processor for the count filter.
 *
 * @param countFilter The count filter to use when processing
 */
class CountFilterProcessor(
  val countFilter: CountFilter
) extends JDIRequestProcessor {
  private val count = countFilter.count

  /**
   * Processes the provided event request with the filter logic.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  override def process(eventRequest: EventRequest): EventRequest = {
    if (eventRequest != null) eventRequest.addCountFilter(count)

    eventRequest
  }

  override val argument: JDIRequestArgument = countFilter
}
