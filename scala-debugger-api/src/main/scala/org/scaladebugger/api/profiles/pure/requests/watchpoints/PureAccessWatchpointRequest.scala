package org.scaladebugger.api.profiles.pure.requests.watchpoints

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.AccessWatchpointEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.lowlevel.watchpoints._
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.AccessWatchpointEventInfo
import org.scaladebugger.api.profiles.traits.requests.watchpoints.AccessWatchpointRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for access watchpoints that adds no
 * extra logic on top of the standard JDI.
 */
trait PureAccessWatchpointRequest extends AccessWatchpointRequest {
  protected val accessWatchpointManager: AccessWatchpointManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newAccessWatchpointRequestHelper()

  /**
   * Constructs a new request helper for access watchpoint.
   *
   * @return The new request helper
   */
  protected def newAccessWatchpointRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Field Name, JDI Request Args)
    // CounterKey: (Class Name, Field Name, JDI Request Args)
    type E = AccessWatchpointEvent
    type EI = AccessWatchpointEventInfo
    type RequestArgs = (String, String, Seq[JDIRequestArgument])
    type CounterKey = (String, String, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = AccessWatchpointEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (className, fieldName, _) = requestArgs
        accessWatchpointManager.createAccessWatchpointRequestWithId(
          requestId,
          className,
          fieldName,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        val (className, fieldName, _) = requestArgs
        accessWatchpointManager.hasAccessWatchpointRequest(className, fieldName)
      },
      _removeRequestById = (requestId) => {
        accessWatchpointManager.removeAccessWatchpointRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultAccessWatchpointEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = accessWatchpointManager.getAccessWatchpointRequestInfoWithId
    )
  }

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
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = (className, fieldName, rArgs)
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
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
}
