package org.scaladebugger.api.profiles.java.requests.methods


import com.sun.jdi.event.MethodEntryEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.MethodEntryEventType
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.lowlevel.methods.{MethodEntryManager, MethodEntryRequestInfo, PendingMethodEntrySupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfo
import org.scaladebugger.api.profiles.traits.requests.methods.MethodEntryRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java profile for method exit that adds no extra logic on top
 * of the standard JDI.
 */
trait JavaMethodEntryRequest extends MethodEntryRequest {
  protected val methodEntryManager: MethodEntryManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMethodEntryRequestHelper()

  /**
   * Constructs a new request helper for method exit.
   *
   * @return The new request helper
   */
  protected def newMethodEntryRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Method Name, JDI Request Args)
    // CounterKey: (Class Name, Method Name, JDI Request Args)
    type E = MethodEntryEvent
    type EI = MethodEntryEventInfo
    type RequestArgs = (String, String, Seq[JDIRequestArgument])
    type CounterKey = (String, String, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MethodEntryEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (className, methodName, _) = requestArgs
        methodEntryManager.createMethodEntryRequestWithId(
          requestId,
          className,
          methodName,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        val (className, methodName, _) = requestArgs
        methodEntryManager.hasMethodEntryRequest(className, methodName)
      },
      _removeRequestById = (requestId) => {
        methodEntryManager.removeMethodEntryRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMethodEntryEventInfo(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = methodEntryManager.getMethodEntryRequestInfoWithId
    )
  }

  /**
   * Retrieves the collection of active and pending method exit requests.
   *
   * @return The collection of information on method exit requests
   */
  override def methodEntryRequests: Seq[MethodEntryRequestInfo] = {
    methodEntryManager.methodEntryRequestList ++ (methodEntryManager match {
      case p: PendingMethodEntrySupportLike  => p.pendingMethodEntryRequests
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
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateMethodEntryRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = (className, methodName, rArgs)
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(
        id,
        MethodNameFilter(methodName) +: eArgs,
        requestArgs
      ))
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
  override def isMethodEntryRequestPending(
    className: String,
    methodName: String
  ): Boolean = methodEntryRequests.filter(m =>
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
   * Removes all method exit requests for the specified class method.
   *
   * @param className  The full name of the class/object/trait containing the
   *                   method being watched
   * @param methodName The name of the method being watched
   * @return The collection of information about removed method exit requests
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
   * Removes all method exit requests.
   *
   * @return The collection of information about removed method exit requests
   */
  override def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo] = {
    methodEntryRequests.filter(m =>
      methodEntryManager.removeMethodEntryRequestWithId(m.requestId)
    )
  }
}
