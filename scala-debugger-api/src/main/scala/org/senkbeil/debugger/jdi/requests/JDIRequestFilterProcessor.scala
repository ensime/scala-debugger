package org.senkbeil.debugger.jdi.requests

import com.sun.jdi.request.EventRequest
import org.senkbeil.debugger.jdi.requests.filters.JDIRequestFilter

/**
 * Represents a processor for filters for JDI Requests.
 *
 * @param filters The collection of filters to use
 */
class JDIRequestFilterProcessor(private val filters: JDIRequestFilter*) {
  /** Contains a collection of processors based on provided filters. */
  private val processors = filters.map(_.toProcessor)

  /**
   * Processes the request, applying any provided request filters.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request instance
   */
  def process(eventRequest: EventRequest): EventRequest = {
    processors.scanLeft(eventRequest)({ case (er, jdiRequestFilter) =>
      jdiRequestFilter.process(er)
    }).last
  }
}
