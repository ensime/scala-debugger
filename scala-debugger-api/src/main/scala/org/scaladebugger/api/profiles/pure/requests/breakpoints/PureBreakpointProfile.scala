package org.scaladebugger.api.profiles.pure.requests.breakpoints
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.breakpoints.{BreakpointManager, BreakpointRequestInfo, PendingBreakpointSupportLike}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for breakpoints that adds no extra logic on top
 * of the standard JDI.
 */
trait PureBreakpointProfile extends BreakpointProfile {
  protected val breakpointManager: BreakpointManager
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
    (String, Int, Seq[JDIEventArgument]),
    AtomicInteger
  ]().asScala

  /**
   * Retrieves the collection of active and pending breakpoint requests.
   *
   * @return The collection of information on breakpoint requests
   */
  override def breakpointRequests: Seq[BreakpointRequestInfo] = {
    breakpointManager.breakpointRequestList ++ (breakpointManager match {
      case p: PendingBreakpointSupportLike  => p.pendingBreakpointRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateBreakpointRequestWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newBreakpointRequest((fileName, lineNumber, rArgs))
    newBreakpointPipeline(requestId, (fileName, lineNumber, eArgs))
  }

  /**
   * Determines if there is any breakpoint on the specified file and line
   * that is pending.
   *
   * @param fileName   The name of the file where the breakpoint resides
   * @param lineNumber The number of the line where the breakpoint resides
   * @return True if there is at least one breakpoint at the specified location
   *         that is pending, otherwise false
   */
  override def isBreakpointRequestPending(
    fileName: String,
    lineNumber: Int
  ): Boolean = breakpointRequests.filter(b =>
    b.fileName == fileName && b.lineNumber == lineNumber
  ).exists(_.isPending)

  /**
   * Determines if the breakpoint with the specified arguments is pending.
   *
   * @param fileName       The name of the file where the breakpoint resides
   * @param lineNumber     The number of the line where the breakpoint resides
   * @param extraArguments The additional arguments provided to the specific
   *                       breakpoint request
   * @return True if there is at least one breakpoint at the specified location
   *         and with the provided extra arguments that is pending,
   *         otherwise false
   */
  override def isBreakpointRequestWithArgsPending(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Boolean = breakpointRequests.filter(b =>
    b.fileName == fileName && b.lineNumber == lineNumber &&
      b.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Removes all breakpoint requests placed on the specified line and file.
   *
   * @param fileName   The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @return The collection of information about removed breakpoint requests
   */
  override def removeBreakpointRequests(
    fileName: String,
    lineNumber: Int
  ): Seq[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests.filter(b =>
      b.fileName == fileName &&
      b.lineNumber == lineNumber
    ))
  }

  /**
   * Removes all breakpoint requests placed on the specified line and file with
   * the specified extra arguments.
   *
   * @param fileName       The name of the file where the breakpoints reside
   * @param lineNumber     The number of the line where the breakpoints reside
   * @param extraArguments the additional arguments provided to the specific
   *                       breakpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeBreakpointRequestWithArgs(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Option[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests.filter(b =>
      b.fileName == fileName &&
      b.lineNumber == lineNumber &&
      b.extraArguments == extraArguments
    )).headOption
  }

  /**
   * Removes all breakpoint requests.
   *
   * @return The collection of information about removed breakpoint requests
   */
  override def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests)
  }

  /**
   * Removes all requests based on the provided information.
   *
   * @param requests The information about the requests to remove
   * @return The information of the removed requests
   */
  private def removeRequestsUsingInfo(
    requests: Seq[BreakpointRequestInfo]
  ): Seq[BreakpointRequestInfo] = {
    requests.filter(r =>
      breakpointManager.removeBreakpointRequestWithId(r.requestId)
    )
  }

  /**
   * Creates a new breakpoint request using the given arguments. The request is
   * memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created breakpoint request
   */
  protected val newBreakpointRequest = {
    type Input = (String, Int, Seq[JDIRequestArgument])
    type Key = (String, Int, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newBreakpointRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        breakpointManager.createBreakpointRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !breakpointManager.hasBreakpointRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of breakpoint events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new breakpoint event and data pipeline
   */
  protected def newBreakpointPipeline(
    requestId: String,
    args: (String, Int, Seq[JDIEventArgument])
  ): IdentityPipeline[BreakpointEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = breakpointManager.getBreakpointRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._3
    val newPipeline = eventManager
      .addEventDataStream(BreakpointEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[BreakpointEvent], t._2))
      .map(t => (eventProducer.newDefaultBreakpointEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[BreakpointEventAndData],
      newBreakpointPipelineCloseFunc(requestId, args)
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
   * @return The new function for closing the pipeline
   */
  protected def newBreakpointPipelineCloseFunc(
    requestId: String,
    args: (String, Int, Seq[JDIEventArgument])
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      breakpointManager.removeBreakpointRequestWithId(requestId)
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
  protected def newBreakpointRequestId(): String =
    java.util.UUID.randomUUID().toString
}
