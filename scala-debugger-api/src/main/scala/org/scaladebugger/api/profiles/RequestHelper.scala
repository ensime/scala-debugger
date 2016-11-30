package org.scaladebugger.api.profiles

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.{JDIArgument, RequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the base request that abstracts functionality common
 * among all requests.
 *
 * @param scalaVirtualMachine The destination for the requests
 * @param eventManager The low-level event manager to listen to events
 * @param etInstance The type of event to
 * @param _newRequestId Generates a new request id
 * @param _newRequest Creates a new request using the provided request id,
 *                    request arguments, and collection of JDI arguments
 * @param _hasRequest Determines whether a request exists with the provided
 *                    request arguments
 * @param _removeRequestById Removes a request using its id
 * @param _newEventInfo Creates a new event info pipeline using the provided
 *                      Scala virtual machine, JDI event, and collection of
 *                      JDI arguments
 * @param _retrieveRequestInfo Retrieves the information for a request using its
 *                           request id, returning Some(info) if found
 * @tparam E The JDI event
 * @tparam EI The event info type to transform the JDI event into
 * @tparam RequestArgs The arguments used to create a request
 * @tparam CounterKey The key to use when looking up a pipeline counter to
 *                    increment or decrement
 */
class RequestHelper[
  E <: Event,
  EI <: EventInfoProfile,
  RequestArgs,
  CounterKey
](
  protected val scalaVirtualMachine: ScalaVirtualMachine,
  protected val eventManager: EventManager,
  private val etInstance: EventType,
  private val _newRequestId: () => String,
  private val _newRequest: (String, RequestArgs, Seq[JDIRequestArgument]) => Try[String],
  private val _hasRequest: (RequestArgs) => Boolean,
  private val _removeRequestById: String => Unit,
  private val _newEventInfo: (ScalaVirtualMachine, E, Seq[JDIArgument]) => EI,
  private val _retrieveRequestInfo: String => Option[RequestInfo]
) {

  /** Represents the combination of event and data returned. */
  type EventAndData = (EI, Seq[JDIEventDataResult])

  /**
   * Contains a mapping of request ids to associated event handler ids.
   */
  private val pipelineRequestEventIds = new MultiMap[String, String]

  /**
   * Contains mapping from input to a counter indicating how many pipelines
   * are currently active for the input.
   */
  private val pipelineCounter =
    new ConcurrentHashMap[CounterKey, AtomicInteger]().asScala

  /**
   * Creates a new request using the given arguments. The
   * request is memoized, meaning that the same request will be returned for
   * the same arguments. The memoized result will be thrown out if the
   * underlying request storage indicates that the request has been removed.
   *
   * @param requestArgs The custom request arguments
   * @param jdiRequestArgs The JDI request arguments
   * @return Success containing the event id, otherwise a failure
   */
  def newRequest(
    requestArgs: RequestArgs,
    jdiRequestArgs: Seq[JDIRequestArgument]
  ) = newRequestImpl(requestArgs, jdiRequestArgs)

  /** Represents the internal implementation of newRequest. */
  private lazy val newRequestImpl = {
    type Input = (RequestArgs, Seq[JDIRequestArgument])
    type Key = (RequestArgs, Seq[JDIRequestArgument])
    type Output = Try[String]

    val m = new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = _newRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._2

        _newRequest(requestId, input._1, args)
      },
      cacheInvalidFunc = (key: Key) => !_hasRequest(key._1)
    )

    (requestArgs: RequestArgs, jdiRequestArgs: Seq[JDIRequestArgument]) =>
      Try(m((requestArgs, jdiRequestArgs))).flatten
  }

  /**
   * Creates a new pipeline of events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param counterKey The key used to increment and decrement the underlying
   *                   pipeline counter
   * @return Success containing new event and data pipeline, otherwise a failure
   */
  def newEventPipeline(
    requestId: String,
    eventArgs: Seq[JDIEventArgument],
    counterKey: CounterKey
  ): Try[IdentityPipeline[EventAndData]] = Try {
    // Lookup final set of request arguments used when creating the request
    val rArgs = _retrieveRequestInfo(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: eventArgs
    val newPipeline = newEventStream(rArgs, eArgsWithFilter)

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[EventAndData],
      newPipelineCloseFunc(requestId, counterKey)
    )
    val combinedPipeline = newPipeline.unionOutput(closePipeline)

    // Increment the counter for open pipelines
    pipelineCounter
      .getOrElseUpdate(counterKey, new AtomicInteger(0))
      .incrementAndGet()

    // Store the new event handler id as associated with the current request
    pipelineRequestEventIds.put(
      requestId,
      combinedPipeline.currentMetadata(
        EventManager.EventHandlerIdMetadataField
      ).asInstanceOf[String]
    )

    combinedPipeline
  }

  /**
   * Creates an event stream pipeline using the provided request and event
   * arguments as input to creating the new event stream.
   *
   * @param requestArgs The request arguments to use when creating the stream
   * @param eventArgs The event arguments to use when creating the stream
   * @return A new pipeline of events and associated data
   */
  private def newEventStream(
    requestArgs: Seq[JDIRequestArgument],
    eventArgs: Seq[JDIEventArgument]
  ): IdentityPipeline[EventAndData] = {
    val allArgs = (requestArgs ++ eventArgs).distinct
    eventManager
      .addEventDataStream(etInstance, eventArgs: _*)
      .map(t => (t._1.asInstanceOf[E], t._2))
      .map(t => (_newEventInfo(scalaVirtualMachine, t._1, allArgs), t._2))
      .noop()
  }

  /**
   * Creates a new function used for closing generated pipelines.
   *
   * @param requestId The id of the request
   * @param counterKey The key used to decrement the underlying pipeline counter
   * @return The new function for closing the pipeline
   */
  private def newPipelineCloseFunc(
    requestId: String,
    counterKey: CounterKey
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(counterKey)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      _removeRequestById(requestId)
      pipelineRequestEventIds.remove(requestId).foreach(
        _.foreach(eventManager.removeEventHandler)
      )
      pCounter.set(0)
    }
  }
}
