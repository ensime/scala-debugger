package org.senkbeil.debugger.api.jdi.requests

import com.sun.jdi.request.EventRequest

/**
 * Represents a processor for arguments for JDI Requests.
 *
 * @param arguments The collection of arguments to use
 */
class JDIRequestArgumentProcessor(private val arguments: JDIRequestArgument*) {
  /** Contains a collection of processors based on provided arguments. */
  private val processors = arguments.map(_.toProcessor)

  /**
   * Processes the request, applying any provided request arguments.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request instance
   */
  def process(eventRequest: EventRequest): EventRequest = {
    processors.scanLeft(eventRequest)({ case (er, jdiRequestArgument) =>
      jdiRequestArgument.process(er)
    }).last
  }
}
