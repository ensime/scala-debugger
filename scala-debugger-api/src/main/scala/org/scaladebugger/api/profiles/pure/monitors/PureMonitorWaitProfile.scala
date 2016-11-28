package org.scaladebugger.api.profiles.pure.monitors
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.MonitorWaitEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.monitors._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.monitors.MonitorWaitProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for monitor wait events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorWaitProfile extends MonitorWaitProfile {
  protected val monitorWaitManager: MonitorWaitManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /**
   * Contains a mapping of request ids to associated event handler ids.
   */
  private val pipelineRequestEventIds = new MultiMap[String, String]

  /**
   * Contains mapping from input to a counter indicating how many pipelines
   * are currently active for the input.
   */
  private val pipelineCounter = new ConcurrentHashMap[
    Seq[JDIArgument],
    AtomicInteger
  ]().asScala

  /**
   * Retrieves the collection of active and pending monitor wait requests.
   *
   * @return The collection of information on monitor wait requests
   */
  override def monitorWaitRequests: Seq[MonitorWaitRequestInfo] = {
    val activeRequests = monitorWaitManager.monitorWaitRequestList.flatMap(
      monitorWaitManager.getMonitorWaitRequestInfo
    )

    activeRequests ++ (monitorWaitManager match {
      case p: PendingMonitorWaitSupportLike => p.pendingMonitorWaitRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateMonitorWaitRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMonitorWaitRequest(rArgs)
    newMonitorWaitPipeline(requestId, eArgs)
  }

  /**
   * Determines if the monitor wait request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor wait request
   * @return True if there is at least one monitor wait request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isMonitorWaitRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    monitorWaitRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all monitor wait requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor wait request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMonitorWaitRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorWaitRequestInfo] = {
    monitorWaitRequests
      .find(_.extraArguments == extraArguments)
      .filter(c =>
        monitorWaitManager.removeMonitorWaitRequest(
          c.requestId
        )
      )
  }

  /**
   * Removes all monitor wait requests.
   *
   * @return The collection of information about removed
   *         monitor wait requests
   */
  override def removeAllMonitorWaitRequests(): Seq[MonitorWaitRequestInfo] = {
    monitorWaitRequests.filter(c =>
      monitorWaitManager.removeMonitorWaitRequest(
        c.requestId
      )
    )
  }

  /**
   * Creates a new monitor wait request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created monitor wait request
   */
  protected val newMonitorWaitRequest = {
    type Input = (Seq[JDIRequestArgument])
    type Key = (Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMonitorWaitRequestId()
        val args = UniqueIdProperty(id = requestId) +: input

        monitorWaitManager.createMonitorWaitRequestWithId(
          requestId,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        // TODO: Remove hard-coded filtering out of UniqueIdProperty,
        //       which shows up in saved arguments since it is passed in
        //       during the memoization above
        !monitorWaitManager.monitorWaitRequestList
          .flatMap(monitorWaitManager.getMonitorWaitRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(key)
      }
    )
  }

  /**
   * Creates a new pipeline of monitor wait events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new monitor wait event and data pipeline
   */
  protected def newMonitorWaitPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[MonitorWaitEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = monitorWaitManager.getMonitorWaitRequestInfo(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    val newPipeline = eventManager
      .addEventDataStream(MonitorWaitEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[MonitorWaitEvent], t._2))
      .map(t => (eventProducer.newDefaultMonitorWaitEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[MonitorWaitEventAndData],
      newMonitorWaitPipelineCloseFunc(requestId, args)
    )

    // Increment the counter for open pipelines
    pipelineCounter
      .getOrElseUpdate(args, new AtomicInteger(0))
      .incrementAndGet()

    val combinedPipeline = newPipeline.unionOutput(closePipeline)

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
   * Creates a new function used for closing generated pipelines.
   *
   * @param requestId The id of the request
   * @param args The arguments associated with the request
   *
   * @return The new function for closing the pipeline
   */
  protected def newMonitorWaitPipelineCloseFunc(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      monitorWaitManager.removeMonitorWaitRequest(requestId)
      pipelineRequestEventIds.remove(requestId).foreach(
        _.foreach(eventManager.removeEventHandler)
      )
      pCounter.set(0)
    }
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newMonitorWaitRequestId(): String =
    java.util.UUID.randomUUID().toString
}
