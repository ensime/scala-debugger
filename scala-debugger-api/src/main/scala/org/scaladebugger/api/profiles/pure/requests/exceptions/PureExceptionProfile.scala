package org.scaladebugger.api.profiles.pure.requests.exceptions
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.exceptions.{ExceptionManager, ExceptionRequestInfo, PendingExceptionSupport, PendingExceptionSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for exceptions that adds no extra logic on
 * top of the standard JDI.
 */
trait PureExceptionProfile extends ExceptionProfile {
  protected val exceptionManager: ExceptionManager
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
    (String, Boolean, Boolean, Seq[JDIEventArgument]),
    AtomicInteger
  ]().asScala

  /**
   * Retrieves the collection of active and pending exceptions requests.
   *
   * @return The collection of information on exception requests
   */
  override def exceptionRequests: Seq[ExceptionRequestInfo] = {
    exceptionManager.exceptionRequestList ++ (exceptionManager match {
      case p: PendingExceptionSupportLike => p.pendingExceptionRequests
      case _                              => Nil
    })
  }

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateAllExceptionsRequestWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newCatchallExceptionRequest((
      notifyCaught,
      notifyUncaught,
      rArgs
    ))

    val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
    newExceptionPipeline(
      requestId,
      (exceptionName, notifyCaught, notifyUncaught, eArgs)
    )
  }

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateExceptionRequestWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = Try {
    require(exceptionName != null, "Exception name cannot be null!")
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newExceptionRequest((
      exceptionName,
      notifyCaught,
      notifyUncaught,
      rArgs
    ))
    newExceptionPipeline(
      requestId,
      (exceptionName, notifyCaught, notifyUncaught, eArgs)
    )
  }

  /**
   * Determines if there is any "all exceptions" request pending.
   *
   * @return True if there is at least one "all exceptions" request pending,
   *         otherwise false
   */
  override def isAllExceptionsRequestPending: Boolean = {
    exceptionRequests.filter(_.isCatchall).exists(_.isPending)
  }

  /**
   * Determines if there is any exception with the specified class name that
   * is pending.
   *
   * @param exceptionName  The full class name of the exception
   * @param notifyCaught   The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments The additional arguments provided to the specific
   *                       exception request
   * @return True if there is at least one exception with the specified class
   *         name, notify caught, notify uncaught, and extra arguments that is
   *         pending, otherwise false
   */
  override def isExceptionRequestWithArgsPending(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean = exceptionRequests.filter(e =>
    e.className == exceptionName &&
    e.notifyCaught == notifyCaught &&
    e.notifyUncaught == notifyUncaught &&
    e.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Determines if there is any exception with the specified class name that
   * is pending.
   *
   * @param exceptionName The full class name of the exception
   * @return True if there is at least one exception with the specified class
   *         name that is pending, otherwise false
   */
  override def isExceptionRequestPending(exceptionName: String): Boolean = {
    exceptionRequests.filter(_.className == exceptionName).exists(_.isPending)
  }

  /**
   * Determines if there is any "all exceptions" request pending with the
   * specified arguments.
   *
   * @param notifyCaught   The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments The additional arguments provided to the specific
   *                       exception request
   * @return True if there is at least one "all exceptions" request with the
   *         specified notify caught, notify uncaught, and extra arguments that
   *         is pending, otherwise false
   */
  override def isAllExceptionsRequestWithArgsPending(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean = exceptionRequests.filter(_.isCatchall).filter(e =>
    e.notifyCaught == notifyCaught &&
    e.notifyUncaught == notifyUncaught &&
    e.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Removes exception requests targeted towards "all exceptions."
   *
   * @return The collection of information about removed exception requests
   */
  override def removeOnlyAllExceptionsRequests(): Seq[ExceptionRequestInfo] = {
    exceptionRequests.filter(_.isCatchall).filter(e =>
      exceptionManager.removeExceptionRequestWithId(e.requestId)
    )
  }

  /**
   * Removes all exception requests with the specified class name.
   *
   * @param exceptionName The full class name of the exception
   * @return The collection of information about removed exception requests
   */
  override def removeExceptionRequests(
    exceptionName: String
  ): Seq[ExceptionRequestInfo] = {
    exceptionRequests.filter(_.className == exceptionName).filter(e =>
      exceptionManager.removeExceptionRequestWithId(e.requestId)
    )
  }

  /**
   * Removes all exception requests.
   *
   * @return The collection of information about removed exception requests
   */
  override def removeAllExceptionRequests(): Seq[ExceptionRequestInfo] = {
    exceptionRequests.filter(e =>
      exceptionManager.removeExceptionRequestWithId(e.requestId)
    )
  }

  /**
   * Remove the exception request with the specified class name, notification
   * flags, and extra arguments.
   *
   * @param exceptionName  The full class name of the exception
   * @param notifyCaught   The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeExceptionRequestWithArgs(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo] = {
    exceptionRequests.find(e =>
      e.className == exceptionName &&
      e.notifyCaught == notifyCaught &&
      e.notifyUncaught == notifyUncaught &&
      e.extraArguments == extraArguments
    ).filter(e => exceptionManager.removeExceptionRequestWithId(e.requestId))
  }

  /**
   * Removes the exception request targeted towards "all exceptions" with
   * the specified notification flags and extra arguments.
   *
   * @param notifyCaught   The caught notification flag provided to the request
   * @param notifyUncaught The uncaught notification flag provided to the request
   * @param extraArguments the additional arguments provided to the specific
   *                       exception request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeOnlyAllExceptionsRequestWithArgs(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo] = {
    exceptionRequests.filter(_.isCatchall).find(e =>
      e.notifyCaught == notifyCaught &&
      e.notifyUncaught == notifyUncaught &&
      e.extraArguments == extraArguments
    ).filter(e => exceptionManager.removeExceptionRequestWithId(e.requestId))
  }

  /**
   * Creates a new exception request using the given arguments. The request is
   * memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created exception request
   */
  protected val newExceptionRequest = {
    type Input = (String, Boolean, Boolean, Seq[JDIRequestArgument])
    type Key = (String, Boolean, Boolean, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newExceptionRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._4

        exceptionManager.createExceptionRequestWithId(
          requestId,
          input._1,
          input._2,
          input._3,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !exceptionManager.hasExceptionRequest(key._1)
      }
    )
  }

  /**
   * Creates a new catchall exception request using the given arguments. The
   * request is memoized, meaning that the same request will be returned for
   * the same arguments. The memoized result will be thrown out if the
   * underlying request storage indicates that the request has been removed.
   *
   * @return The id of the created exception request
   */
  protected val newCatchallExceptionRequest = {
    type Input = (Boolean, Boolean, Seq[JDIRequestArgument])
    type Key = (Boolean, Boolean, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newExceptionRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        exceptionManager.createCatchallExceptionRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        // Key notifyCaught, key notifyUncaught
        val knc = key._1
        val knu = key._2

        // TODO: Remove hack to exclude unique id property for matches
        val kea = key._3.filterNot(_.isInstanceOf[UniqueIdProperty])
        val keas = kea.toSet

        !exceptionRequests.exists {
          case ExceptionRequestInfo(_, _, cn, nc, nu, ea) =>
            // TODO: Support denying when same element multiple times as set
            //       removes duplicates
            val eas = ea.filterNot(_.isInstanceOf[UniqueIdProperty]).toSet
            cn == ExceptionRequestInfo.DefaultCatchallExceptionName &&
            nc == knc &&
            nu == knu &&
            // TODO: Improve checking elements
            // Same elements in any order
            eas == keas
        }
      }
    )
  }

  /**
   * Creates a new pipeline of exception events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new exception event and data pipeline
   */
  protected def newExceptionPipeline(
    requestId: String,
    args: (String, Boolean, Boolean, Seq[JDIEventArgument])
  ): IdentityPipeline[ExceptionEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = exceptionManager.getExceptionRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)

    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._4
    val newPipeline = eventManager
      .addEventDataStream(ExceptionEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[ExceptionEvent], t._2))
      .map(t => (eventProducer.newDefaultExceptionEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgsWithFilter: _*
      ), t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[ExceptionEventAndData],
      newExceptionPipelineCloseFunc(requestId, args)
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
  protected def newExceptionPipelineCloseFunc(
    requestId: String,
    args: (String, Boolean, Boolean, Seq[JDIEventArgument])
  ): (Option[Any]) => Unit = (data: Option[Any]) => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0 || data.exists(_ == CloseRemoveAll)) {
      exceptionManager.removeExceptionRequestWithId(requestId)
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
  protected def newExceptionRequestId(): String =
    java.util.UUID.randomUUID().toString
}
