package org.scaladebugger.api.lowlevel.events.data.processors
//import acyclic.file

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.data.{JDIEventDataProcessor, JDIEventDataRequest, JDIEventDataResult}
import org.scaladebugger.api.lowlevel.events.data.requests.CustomPropertyDataRequest
import org.scaladebugger.api.lowlevel.events.data.results.CustomPropertyDataResult

/**
 * Represents a processor for the custom property data request.
 *
 * @param customPropertyDataRequest The data request to use when processing
 */
class CustomPropertyDataRequestProcessor(
  val customPropertyDataRequest: CustomPropertyDataRequest
) extends JDIEventDataProcessor {
  private val key = customPropertyDataRequest.key

  /**
   * Processes the provided event to retrieve the requested data.
   *
   * @param event The event to process
   *
   * @return The collection of results from processing the event
   */
  override def process(event: Event): Seq[JDIEventDataResult] = {
    val result = Option(event.request())
      .flatMap(r => Option(r.getProperty(key)))
      .map(v => CustomPropertyDataResult(key, v))

    result.toSeq
  }

  override val argument: JDIEventDataRequest = customPropertyDataRequest
}
