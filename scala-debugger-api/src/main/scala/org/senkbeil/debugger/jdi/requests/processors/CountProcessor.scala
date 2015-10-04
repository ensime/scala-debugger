package org.senkbeil.debugger.jdi.requests.processors

import com.sun.jdi.request.EventRequest
import org.senkbeil.debugger.jdi.requests.filters.{CountFilter, JDIRequestFilter}

/**
 * Represents a processor for the count filter.
 *
 * @param countFilter The count filter to use when processing
 */
class CountProcessor(
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

  override val filter: JDIRequestFilter = countFilter
}
