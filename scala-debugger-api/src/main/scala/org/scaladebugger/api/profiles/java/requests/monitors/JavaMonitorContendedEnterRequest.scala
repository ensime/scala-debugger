package org.scaladebugger.api.profiles.java.requests.monitors


import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnterManager, MonitorContendedEnterRequestInfo, PendingMonitorContendedEnterSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnterEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnterRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java profile for monitor contended enter events that adds no
 * extra logic on top of the standard JDI.
 */
trait JavaMonitorContendedEnterRequest extends MonitorContendedEnterRequest {
  protected val monitorContendedEnterManager: MonitorContendedEnterManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMonitorContendedEnterRequestHelper()

  /**
   * Constructs a new request helper for monitor contended enter.
   *
   * @return The new request helper
   */
  protected def newMonitorContendedEnterRequestHelper() = {
    type E = MonitorContendedEnterEvent
    type EI = MonitorContendedEnterEventInfo
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MonitorContendedEnterEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        monitorContendedEnterManager.createMonitorContendedEnterRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        monitorContendedEnterManager.monitorContendedEnterRequestList
          .flatMap(monitorContendedEnterManager.getMonitorContendedEnterRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        monitorContendedEnterManager.removeMonitorContendedEnterRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMonitorContendedEnterEventInfo(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = monitorContendedEnterManager.getMonitorContendedEnterRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending monitor contended enter
   * requests.
   *
   * @return The collection of information on monitor contended enter requests
   */
  override def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = {
    val activeRequests = monitorContendedEnterManager.monitorContendedEnterRequestList.flatMap(
      monitorContendedEnterManager.getMonitorContendedEnterRequestInfo
    )

    activeRequests ++ (monitorContendedEnterManager match {
      case p: PendingMonitorContendedEnterSupportLike => p.pendingMonitorContendedEnterRequests
      case _                                            => Nil
    })
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  override def tryGetOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the monitor contended enter request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor contended enter request
   * @return True if there is at least one monitor contended enter request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isMonitorContendedEnterRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    monitorContendedEnterRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all monitor contended enter requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended enter request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMonitorContendedEnterRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnterRequestInfo] = {
    monitorContendedEnterRequests
      .find(_.extraArguments == extraArguments)
      .filter(c =>
        monitorContendedEnterManager.removeMonitorContendedEnterRequest(
          c.requestId
        )
      )
  }

  /**
   * Removes all monitor contended enter requests.
   *
   * @return The collection of information about removed
   *         monitor contended enter requests
   */
  override def removeAllMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo] = {
    monitorContendedEnterRequests.filter(c =>
      monitorContendedEnterManager.removeMonitorContendedEnterRequest(
        c.requestId
      )
    )
  }
}

