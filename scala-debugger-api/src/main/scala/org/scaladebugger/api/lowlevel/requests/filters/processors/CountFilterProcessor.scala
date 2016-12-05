package org.scaladebugger.api.lowlevel.requests.filters.processors

import com.sun.jdi.request.EventRequest
import org.scaladebugger.api.lowlevel.requests.filters.{JDIRequestFilter, JDIRequestFilterProcessor, CountFilter}

/**
 * Represents a processor for the count filter.
 *
 * @param countFilter The count filter to use when processing
 */
class CountFilterProcessor(
  val countFilter: CountFilter
) extends JDIRequestFilterProcessor {
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

  override val argument: JDIRequestFilter = countFilter
}
