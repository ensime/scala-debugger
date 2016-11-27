package org.scaladebugger.api.profiles.pure.threads
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.ThreadStartEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.threads._
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.threads.ThreadStartProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.lowlevel.events.EventType.ThreadStartEventType
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadStartProfile extends ThreadStartProfile {
  protected val threadStartManager: ThreadStartManager
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
   * Retrieves the collection of active and pending thread start requests.
   *
   * @return The collection of information on thread start requests
   */
  override def threadStartRequests: Seq[ThreadStartRequestInfo] = {
    val activeRequests = threadStartManager.threadStartRequestList.flatMap(
      threadStartManager.getThreadStartRequestInfo
    )

    activeRequests ++ (threadStartManager match {
      case p: PendingThreadStartSupportLike => p.pendingThreadStartRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateThreadStartRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newThreadStartRequest(rArgs)
    newThreadStartPipeline(requestId, eArgs)
  }

  /**
   * Determines if the thread start request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       thread start request
   * @return True if there is at least one thread start request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isThreadStartRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    threadStartRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all thread start requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread start request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeThreadStartRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadStartRequestInfo] = {
    threadStartRequests.find(_.extraArguments == extraArguments).filter(c =>
      threadStartManager.removeThreadStartRequest(c.requestId)
    )
  }

  /**
   * Removes all thread start requests.
   *
   * @return The collection of information about removed thread start requests
   */
  override def removeAllThreadStartRequests(): Seq[ThreadStartRequestInfo] = {
    threadStartRequests.filter(c =>
      threadStartManager.removeThreadStartRequest(c.requestId)
    )
  }

  /**
   * Creates a new thread start request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created thread start request
   */
  protected val newThreadStartRequest = {
    type Input = (Seq[JDIRequestArgument])
    type Key = (Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newThreadStartRequestId()
        val args = UniqueIdProperty(id = requestId) +: input

        threadStartManager.createThreadStartRequestWithId(
          requestId,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        // TODO: Remove hard-coded filtering out of UniqueIdProperty,
        //       which shows up in saved arguments since it is passed in
        //       during the memoization above
        !threadStartManager.threadStartRequestList
          .flatMap(threadStartManager.getThreadStartRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(key)
      }
    )
  }

  /**
   * Creates a new pipeline of thread start events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new thread start event and data pipeline
   */
  protected def newThreadStartPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[ThreadStartEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = threadStartManager.getThreadStartRequestInfo(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    val newPipeline = eventManager
      .addEventDataStream(ThreadStartEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[ThreadStartEvent], t._2))
      .map(t => (eventProducer.newThreadStartEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      )(), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[ThreadStartEventAndData],
      newThreadStartPipelineCloseFunc(requestId, args)
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
  protected def newThreadStartPipelineCloseFunc(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      threadStartManager.removeThreadStartRequest(requestId)
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
  protected def newThreadStartRequestId(): String =
    java.util.UUID.randomUUID().toString
}
