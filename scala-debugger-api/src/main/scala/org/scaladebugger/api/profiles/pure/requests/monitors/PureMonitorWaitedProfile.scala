package org.scaladebugger.api.profiles.pure.requests.monitors

//import acyclic.file

import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitedManager, MonitorWaitedRequestInfo, PendingMonitorWaitedSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorWaitedProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for monitor waited events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorWaitedProfile extends MonitorWaitedProfile {
  protected val monitorWaitedManager: MonitorWaitedManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMonitorWaitedRequestHelper()

  /**
   * Constructs a new request helper for access watchpoint.
   *
   * @return The new request helper
   */
  protected def newMonitorWaitedRequestHelper() = {
    type E = MonitorWaitedEvent
    type EI = MonitorWaitedEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MonitorWaitedEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        monitorWaitedManager.createMonitorWaitedRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        monitorWaitedManager.monitorWaitedRequestList
          .flatMap(monitorWaitedManager.getMonitorWaitedRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        monitorWaitedManager.removeMonitorWaitedRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMonitorWaitedEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = monitorWaitedManager.getMonitorWaitedRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending monitor waited
   * requests.
   *
   * @return The collection of information on monitor waited requests
   */
  override def monitorWaitedRequests: Seq[MonitorWaitedRequestInfo] = {
    val activeRequests = monitorWaitedManager.monitorWaitedRequestList.flatMap(
      monitorWaitedManager.getMonitorWaitedRequestInfo
    )

    activeRequests ++ (monitorWaitedManager match {
      case p: PendingMonitorWaitedSupportLike => p.pendingMonitorWaitedRequests
      case _                                            => Nil
    })
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  override def tryGetOrCreateMonitorWaitedRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the monitor waited request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor waited request
   * @return True if there is at least one monitor waited request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isMonitorWaitedRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    monitorWaitedRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all monitor waited requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor waited request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMonitorWaitedRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorWaitedRequestInfo] = {
    monitorWaitedRequests
      .find(_.extraArguments == extraArguments)
      .filter(c =>
        monitorWaitedManager.removeMonitorWaitedRequest(
          c.requestId
        )
      )
  }

  /**
   * Removes all monitor waited requests.
   *
   * @return The collection of information about removed
   *         monitor waited requests
   */
  override def removeAllMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo] = {
    monitorWaitedRequests.filter(c =>
      monitorWaitedManager.removeMonitorWaitedRequest(
        c.requestId
      )
    )
  }
}

