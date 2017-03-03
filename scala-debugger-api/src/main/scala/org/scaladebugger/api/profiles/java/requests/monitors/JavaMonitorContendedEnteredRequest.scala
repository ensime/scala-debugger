package org.scaladebugger.api.profiles.java.requests.monitors

import com.sun.jdi.event.MonitorContendedEnteredEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnteredManager, MonitorContendedEnteredRequestInfo, PendingMonitorContendedEnteredSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnteredEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnteredRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java profile for monitor contended entered events that adds no
 * extra logic on top of the standard JDI.
 */
trait JavaMonitorContendedEnteredRequest extends MonitorContendedEnteredRequest {
  protected val monitorContendedEnteredManager: MonitorContendedEnteredManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMonitorContendedEnteredRequestHelper()

  /**
   * Constructs a new request helper for monitor contended entered.
   *
   * @return The new request helper
   */
  protected def newMonitorContendedEnteredRequestHelper() = {
    type E = MonitorContendedEnteredEvent
    type EI = MonitorContendedEnteredEventInfo
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MonitorContendedEnteredEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        monitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        monitorContendedEnteredManager.monitorContendedEnteredRequestList
          .flatMap(monitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMonitorContendedEnteredEventInfo(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = monitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending monitor contended entered
   * requests.
   *
   * @return The collection of information on monitor contended entered requests
   */
  override def monitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo] = {
    val activeRequests = monitorContendedEnteredManager.monitorContendedEnteredRequestList.flatMap(
      monitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo
    )

    activeRequests ++ (monitorContendedEnteredManager match {
      case p: PendingMonitorContendedEnteredSupportLike => p.pendingMonitorContendedEnteredRequests
      case _                                            => Nil
    })
  }

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of monitor contended entered events and any retrieved
   *         data based on requests from extra arguments
   */
  override def tryGetOrCreateMonitorContendedEnteredRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the monitor contended entered request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor contended entered request
   * @return True if there is at least one monitor contended entered request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isMonitorContendedEnteredRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    monitorContendedEnteredRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all monitor contended entered requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended entered request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMonitorContendedEnteredRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnteredRequestInfo] = {
    monitorContendedEnteredRequests
      .find(_.extraArguments == extraArguments)
      .filter(c =>
        monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(
          c.requestId
        )
      )
  }

  /**
   * Removes all monitor contended entered requests.
   *
   * @return The collection of information about removed
   *         monitor contended entered requests
   */
  override def removeAllMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo] = {
    monitorContendedEnteredRequests.filter(c =>
      monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(
        c.requestId
      )
    )
  }
}

