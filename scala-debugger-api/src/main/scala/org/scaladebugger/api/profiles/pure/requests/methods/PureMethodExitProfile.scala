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
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfoProfile
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

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMethodExitRequestHelper()

  /**
   * Constructs a new request helper for method exit.
   *
   * @return The new request helper
   */
  protected def newMethodExitRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Method Name, JDI Request Args)
    // CounterKey: (Class Name, Method Name, JDI Request Args)
    type E = MethodExitEvent
    type EI = MethodExitEventInfoProfile
    type RequestArgs = (String, String, Seq[JDIRequestArgument])
    type CounterKey = (String, String, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MethodExitEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (className, methodName, _) = requestArgs
        methodExitManager.createMethodExitRequestWithId(
          requestId,
          className,
          methodName,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        val (className, methodName, _) = requestArgs
        methodExitManager.hasMethodExitRequest(className, methodName)
      },
      _removeRequestById = (requestId) => {
        methodExitManager.removeMethodExitRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMethodExitEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = methodExitManager.getMethodExitRequestInfoWithId
    )
  }

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
  ): Try[IdentityPipeline[MethodExitEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = (className, methodName, rArgs)
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
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
}
