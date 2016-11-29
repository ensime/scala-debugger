package org.scaladebugger.api.profiles.pure.requests.watchpoints
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.AccessWatchpointEventType
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.lowlevel.watchpoints._
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.requests.watchpoints.AccessWatchpointProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for access watchpoints that adds no
 * extra logic on top of the standard JDI.
 */
trait PureAccessWatchpointProfile extends AccessWatchpointProfile {
  protected val accessWatchpointManager: AccessWatchpointManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /**
   * Retrieves the collection of active and pending access watchpoint requests.
   *
   * @return The collection of information on access watchpoint requests
   */
  override def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = {
    accessWatchpointManager.accessWatchpointRequestList ++ (accessWatchpointManager match {
      case p: PendingAccessWatchpointSupportLike  => p.pendingAccessWatchpointRequests
      case _                                      => Nil
    })
  }

  /**
   * Contains a mapping of request ids to associated event handler ids.
   */
  private val pipelineRequestEventIds = new MultiMap[String, String]

  /**
   * Contains mapping from input to a counter indicating how many pipelines
   * are currently active for the input.
   */
  private val pipelineCounter = new ConcurrentHashMap[
    (String, String, Seq[JDIEventArgument]),
    AtomicInteger
  ]().asScala

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  override def tryGetOrCreateAccessWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newAccessWatchpointRequest((className, fieldName, rArgs))
    newAccessWatchpointPipeline(requestId, (className, fieldName, eArgs))
  }

  /**
   * Determines if there is any access watchpoint request for the specified
   * class field that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return True if there is at least one access watchpoint request with the
   *         specified field namename in the specified class that is pending,
   *         otherwise false
   */
  override def isAccessWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean = {
    accessWatchpointRequests.filter(a =>
      a.className == className &&
      a.fieldName == fieldName
    ).exists(_.isPending)
  }

  /**
   * Determines if there is any access watchpoint request for the specified
   * class field with matching arguments that is pending.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       field being watched
   * @param fieldName      The name of the field being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       access watchpoint request
   * @return True if there is at least one access watchpoint request with the
   *         specified field name and arguments in the specified class that is
   *         pending, otherwise false
   */
  override def isAccessWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    accessWatchpointRequests.filter(a =>
      a.className == className &&
      a.fieldName == fieldName &&
      a.extraArguments == extraArguments
    ).exists(_.isPending)
  }

  /**
   * Removes all access watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return The collection of information about removed access watchpoint requests
   */
  override def removeAccessWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[AccessWatchpointRequestInfo] = {
    accessWatchpointRequests.filter(a =>
      a.className == className &&
      a.fieldName == fieldName
    ).filter(a =>
      accessWatchpointManager.removeAccessWatchpointRequestWithId(a.requestId)
    )
  }

  /**
   * Removes all access watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       field being watched
   * @param fieldName      The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       access watchpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeAccessWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[AccessWatchpointRequestInfo] = {
    accessWatchpointRequests.find(a =>
      a.className == className &&
      a.fieldName == fieldName &&
      a.extraArguments == extraArguments
    ).filter(a =>
      accessWatchpointManager.removeAccessWatchpointRequestWithId(a.requestId)
    )
  }

  /**
   * Removes all access watchpoint requests.
   *
   * @return The collection of information about removed access watchpoint requests
   */
  override def removeAllAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo] = {
    accessWatchpointRequests.filter(a =>
      accessWatchpointManager.removeAccessWatchpointRequestWithId(a.requestId)
    )
  }

  /**
   * Creates a new access watchpoint request using the given arguments. The
   * request is memoized, meaning that the same request will be returned for
   * the same arguments. The memoized result will be thrown out if the
   * underlying request storage indicates that the request has been removed.
   *
   * @return The id of the created access watchpoint request
   */
  protected val newAccessWatchpointRequest = {
    type Input = (String, String, Seq[JDIRequestArgument])
    type Key = (String, String, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newAccessWatchpointRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        accessWatchpointManager.createAccessWatchpointRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !accessWatchpointManager.hasAccessWatchpointRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of access watchpoint events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new access watchpoint event and data pipeline
   */
  protected def newAccessWatchpointPipeline(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
    ): IdentityPipeline[AccessWatchpointEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = accessWatchpointManager.getAccessWatchpointRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._3
    val newPipeline = eventManager
      .addEventDataStream(AccessWatchpointEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[AccessWatchpointEvent], t._2))
      .map(t => (eventProducer.newDefaultAccessWatchpointEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[AccessWatchpointEventAndData],
      newAccessWatchpointPipelineCloseFunc(requestId, args)
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
  protected def newAccessWatchpointPipelineCloseFunc(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      accessWatchpointManager.removeAccessWatchpointRequestWithId(requestId)
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
  protected def newAccessWatchpointRequestId(): String =
    java.util.UUID.randomUUID().toString
}
