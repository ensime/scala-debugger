package org.scaladebugger.api.profiles.pure.requests.methods
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.MethodExitEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.MethodExitEventType
import org.scaladebugger.api.lowlevel.events.filters.{MethodNameFilter, UniqueIdPropertyFilter}
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.methods.{MethodExitManager, MethodExitRequestInfo, PendingMethodExitSupport, PendingMethodExitSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.requests.methods.MethodExitProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for method exit that adds no extra logic on top
 * of the standard JDI.
 */
trait PureMethodExitProfile extends MethodExitProfile {
  protected val methodExitManager: MethodExitManager
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
    (String, String, Seq[JDIArgument]),
    AtomicInteger
  ]().asScala

  /**
   * Retrieves the collection of active and pending method exit requests.
   *
   * @return The collection of information on method exit requests
   */
  override def methodExitRequests: Seq[MethodExitRequestInfo] = {
    methodExitManager.methodExitRequestList ++ (methodExitManager match {
      case p: PendingMethodExitSupportLike  => p.pendingMethodExitRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMethodExitRequest((className, methodName, rArgs))
    newMethodExitPipeline(
      requestId,
      (className, methodName, MethodNameFilter(methodName) +: eArgs)
    )
  }

  /**
   * Determines if there is any method exit request for the specified class
   * method that is pending.
   *
   * @param className  The full name of the class/object/trait containing the
   *                   method being watched
   * @param methodName The name of the method being watched
   * @return True if there is at least one method exit request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  override def isMethodExitRequestPending(
    className: String,
    methodName: String
  ): Boolean = methodExitRequests.filter(m =>
    m.className == className &&
      m.methodName == methodName
  ).exists(_.isPending)

  /**
   * Determines if there is any method exit request for the specified class
   * method with matching arguments that is pending.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       method being watched
   * @param methodName     The name of the method being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       method exit request
   * @return True if there is at least one method exit request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  override def isMethodExitRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean = methodExitRequests.filter(m =>
    m.className == className &&
      m.methodName == methodName &&
      m.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Removes all method exit requests for the specified class method.
   *
   * @param className  The full name of the class/object/trait containing the
   *                   method being watched
   * @param methodName The name of the method being watched
   * @return The collection of information about removed method exit requests
   */
  override def removeMethodExitRequests(
    className: String,
    methodName: String
  ): Seq[MethodExitRequestInfo] = {
    methodExitRequests.filter(m =>
      m.className == className &&
        m.methodName == methodName
    ).filter(m =>
      methodExitManager.removeMethodExitRequestWithId(m.requestId)
    )
  }

  /**
   * Removes all method exit requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       method being watched
   * @param methodName     The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method exit request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMethodExitRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodExitRequestInfo] = {
    methodExitRequests.find(m =>
      m.className == className &&
        m.methodName == methodName &&
        m.extraArguments == extraArguments
    ).filter(m =>
      methodExitManager.removeMethodExitRequestWithId(m.requestId)
    )
  }

  /**
   * Removes all method exit requests.
   *
   * @return The collection of information about removed method exit requests
   */
  override def removeAllMethodExitRequests(): Seq[MethodExitRequestInfo] = {
    methodExitRequests.filter(m =>
      methodExitManager.removeMethodExitRequestWithId(m.requestId)
    )
  }

  /**
   * Creates a new method exit request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created method exit request
   */
  protected val newMethodExitRequest = {
    type Input = (String, String, Seq[JDIRequestArgument])
    type Key = (String, String, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMethodExitRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        methodExitManager.createMethodExitRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !methodExitManager.hasMethodExitRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of method exit events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new method exit event and data pipeline
   */
  protected def newMethodExitPipeline(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): IdentityPipeline[MethodExitEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = methodExitManager.getMethodExitRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._3
    val newPipeline = eventManager
      .addEventDataStream(MethodExitEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[MethodExitEvent], t._2))
      .map(t => (eventProducer.newDefaultMethodExitEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[MethodExitEventAndData],
      newMethodExitPipelineCloseFunc(requestId, args)
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
  protected def newMethodExitPipelineCloseFunc(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      methodExitManager.removeMethodExitRequestWithId(requestId)
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
  protected def newMethodExitRequestId(): String =
    java.util.UUID.randomUUID().toString
}
