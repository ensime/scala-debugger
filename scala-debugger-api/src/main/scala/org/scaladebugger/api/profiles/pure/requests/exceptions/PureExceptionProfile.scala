package org.scaladebugger.api.profiles.pure.requests.exceptions
//import acyclic.file

import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.exceptions.{ExceptionManager, ExceptionRequestInfo, PendingExceptionSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

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

  /** Represents helper utility to create/manage standard requests. */
  private lazy val standardRequestHelper = newExceptionRequestHelper(false)

  /** Represents helper utility to create/manage catchall requests. */
  private lazy val catchallRequestHelper = newExceptionRequestHelper(true)

  /**
   * Constructs a new request helper for method exit.
   *
   * @param forCatchall If true, generates a request helper for catchall
   *                    exception request handling, otherwise generates a
   *                    request helper for standard request handling
   * @return The new request helper
   */
  protected def newExceptionRequestHelper(forCatchall: Boolean) = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Exception Class Name, Notify Caught, Notify Uncaught, JDI Request Args)
    // CounterKey: (Exception Class Name, Notify Caught, Notify Uncaught, JDI Request Args)
    type E = ExceptionEvent
    type EI = ExceptionEventInfoProfile
    type RequestArgs = (String, Boolean, Boolean, Seq[JDIRequestArgument])
    type CounterKey = (String, Boolean, Boolean, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ExceptionEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (exceptionName, notifyCaught, notifyUncaught, _) = requestArgs
        if (forCatchall) {
          exceptionManager.createCatchallExceptionRequestWithId(
            requestId,
            notifyCaught,
            notifyUncaught,
            jdiRequestArgs: _*
          )
        } else {
          exceptionManager.createExceptionRequestWithId(
            requestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            jdiRequestArgs: _*
          )
        }
      },
      _hasRequest = (requestArgs) => {
        val (exceptionName, notifyCaught, notifyUncaught, jdiArgs) = requestArgs
        if (forCatchall) {
          // TODO: Remove hack to exclude unique id property for matches
          val argsSet = jdiArgs.filterNot(_.isInstanceOf[UniqueIdProperty]).toSet

          exceptionRequests.exists {
            case ExceptionRequestInfo(_, _, cn, nc, nu, ea) =>
              // TODO: Support denying when same element multiple times as set
              //       removes duplicates
              val eas = ea.filterNot(_.isInstanceOf[UniqueIdProperty]).toSet
              cn == ExceptionRequestInfo.DefaultCatchallExceptionName &&
              nc == notifyCaught &&
              nu == notifyUncaught &&
              // TODO: Improve checking elements
              // Same elements in any order
              eas == argsSet
          }
        } else {
          exceptionManager.hasExceptionRequest(exceptionName)
        }
      },
      _removeRequestById = (requestId) => {
        exceptionManager.removeExceptionRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultExceptionEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = exceptionManager.getExceptionRequestInfoWithId
    )
  }

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
  ): Try[IdentityPipeline[ExceptionEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName

    catchallRequestHelper.newRequest((
      exceptionName,
      notifyCaught,
      notifyUncaught,
      rArgs
    ), rArgs).flatMap(requestId => {
      catchallRequestHelper.newEventPipeline(
        requestId,
        eArgs,
        (exceptionName, notifyCaught, notifyUncaught, rArgs)
      )
    })
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

    standardRequestHelper.newRequest((
      exceptionName,
      notifyCaught,
      notifyUncaught,
      rArgs
    ), rArgs).flatMap(requestId => {
      standardRequestHelper.newEventPipeline(
        requestId,
        eArgs,
        (exceptionName, notifyCaught, notifyUncaught, rArgs)
      )
    }).get
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
}
