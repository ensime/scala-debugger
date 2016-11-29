package org.scaladebugger.api.profiles.pure.requests.methods
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.MethodEntryEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.MethodEntryEventType
import org.scaladebugger.api.lowlevel.events.filters.{MethodNameFilter, UniqueIdPropertyFilter}
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.methods._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.requests.methods.MethodEntryProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for method entry that adds no extra logic on top
 * of the standard JDI.
 */
trait PureMethodEntryProfile extends MethodEntryProfile {
  protected val methodEntryManager: MethodEntryManager
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
   * Retrieves the collection of active and pending method entry requests.
   *
   * @return The collection of information on method entry requests
   */
  override def methodEntryRequests: Seq[MethodEntryRequestInfo] = {
    methodEntryManager.methodEntryRequestList ++ (methodEntryManager match {
      case p: PendingMethodEntrySupportLike => p.pendingMethodEntryRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of method entry events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateMethodEntryRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMethodEntryRequest((className, methodName, rArgs))
    newMethodEntryPipeline(
      requestId,
      (className, methodName, MethodNameFilter(methodName) +: eArgs)
    )
  }

  /**
   * Determines if there is any method entry request for the specified class
   * method that is pending.
   *
   * @param className  The full name of the class/object/trait containing the
   *                   method being watched
   * @param methodName The name of the method being watched
   * @return True if there is at least one method entry request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  override def isMethodEntryRequestPending(
    className: String,
    methodName: String
  ): Boolean = methodEntryRequests.filter(m =>
    m.className == className &&
    m.methodName == methodName
  ).exists(_.isPending)

  /**
   * Determines if there is any method entry request for the specified class
   * method with matching arguments that is pending.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       method being watched
   * @param methodName     The name of the method being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       method entry request
   * @return True if there is at least one method entry request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  override def isMethodEntryRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean = methodEntryRequests.filter(m =>
    m.className == className &&
    m.methodName == methodName &&
    m.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Removes all method entry requests for the specified class method.
   *
   * @param className  The full name of the class/object/trait containing the
   *                   method being watched
   * @param methodName The name of the method being watched
   * @return The collection of information about removed method entry requests
   */
  override def removeMethodEntryRequests(
    className: String,
    methodName: String
  ): Seq[MethodEntryRequestInfo] = {
    methodEntryRequests.filter(m =>
      m.className == className &&
      m.methodName == methodName
    ).filter(m =>
      methodEntryManager.removeMethodEntryRequestWithId(m.requestId)
    )
  }

  /**
   * Removes all method entry requests for the specified class method with
   * the specified extra arguments.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       method being watched
   * @param methodName     The name of the method being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       method entry request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMethodEntryRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodEntryRequestInfo] = {
    methodEntryRequests.find(m =>
      m.className == className &&
      m.methodName == methodName &&
      m.extraArguments == extraArguments
    ).filter(m =>
      methodEntryManager.removeMethodEntryRequestWithId(m.requestId)
    )
  }

  /**
   * Removes all method entry requests.
   *
   * @return The collection of information about removed method entry requests
   */
  override def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo] = {
    methodEntryRequests.filter(m =>
      methodEntryManager.removeMethodEntryRequestWithId(m.requestId)
    )
  }

  /**
   * Creates a new method entry request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created method entry request
   */
  protected val newMethodEntryRequest = {
    type Input = (String, String, Seq[JDIRequestArgument])
    type Key = (String, String, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMethodEntryRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        methodEntryManager.createMethodEntryRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !methodEntryManager.hasMethodEntryRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of method entry events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new method entry event and data pipeline
   */
  protected def newMethodEntryPipeline(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): IdentityPipeline[MethodEntryEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = methodEntryManager.getMethodEntryRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._3
    val newPipeline = eventManager
      .addEventDataStream(MethodEntryEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[MethodEntryEvent], t._2))
      .map(t => (eventProducer.newDefaultMethodEntryEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[MethodEntryEventAndData],
      newMethodEntryPipelineCloseFunc(requestId, args)
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
  protected def newMethodEntryPipelineCloseFunc(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      methodEntryManager.removeMethodEntryRequestWithId(requestId)
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
  protected def newMethodEntryRequestId(): String =
    java.util.UUID.randomUUID().toString
}

