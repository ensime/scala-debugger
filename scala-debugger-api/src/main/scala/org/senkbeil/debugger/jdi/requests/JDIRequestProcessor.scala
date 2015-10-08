package org.senkbeil.debugger.jdi.requests

import com.sun.jdi.request.EventRequest

/**
  * Represents a processor for a JDI Request.
  */
trait JDIRequestProcessor {
  /**
   * Processes the provided event request.
   *
   * @param eventRequest The request to process
   *
   * @return The updated request
   */
  def process(eventRequest: EventRequest): EventRequest

  /**
   * Represents the filter contained by this processor.
   *
   * @return The specific filter instance
   */
  val argument: JDIRequestArgument
}
