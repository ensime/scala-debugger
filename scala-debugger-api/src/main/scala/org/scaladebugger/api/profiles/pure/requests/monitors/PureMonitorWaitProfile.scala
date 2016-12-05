package org.scaladebugger.api.profiles.pure.requests.monitors


import com.sun.jdi.event.MonitorWaitEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitManager, MonitorWaitRequestInfo, PendingMonitorWaitSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorWaitProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for monitor wait events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorWaitProfile extends MonitorWaitProfile {
  protected val monitorWaitManager: MonitorWaitManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newMonitorWaitRequestHelper()

  /**
   * Constructs a new request helper for monitor wait.
   *
   * @return The new request helper
   */
  protected def newMonitorWaitRequestHelper() = {
    type E = MonitorWaitEvent
    type EI = MonitorWaitEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = MonitorWaitEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        monitorWaitManager.createMonitorWaitRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        monitorWaitManager.monitorWaitRequestList
          .flatMap(monitorWaitManager.getMonitorWaitRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        monitorWaitManager.removeMonitorWaitRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultMonitorWaitEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = monitorWaitManager.getMonitorWaitRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending monitor wait
   * requests.
   *
   * @return The collection of information on monitor wait requests
   */
  override def monitorWaitRequests: Seq[MonitorWaitRequestInfo] = {
    val activeRequests = monitorWaitManager.monitorWaitRequestList.flatMap(
      monitorWaitManager.getMonitorWaitRequestInfo
    )

    activeRequests ++ (monitorWaitManager match {
      case p: PendingMonitorWaitSupportLike => p.pendingMonitorWaitRequests
      case _                                            => Nil
    })
  }

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of monitor wait events and any retrieved
   *         data based on requests from extra arguments
   */
  override def tryGetOrCreateMonitorWaitRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the monitor wait request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor wait request
   * @return True if there is at least one monitor wait request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isMonitorWaitRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    monitorWaitRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all monitor wait requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor wait request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeMonitorWaitRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorWaitRequestInfo] = {
    monitorWaitRequests
      .find(_.extraArguments == extraArguments)
      .filter(c =>
        monitorWaitManager.removeMonitorWaitRequest(
          c.requestId
        )
      )
  }

  /**
   * Removes all monitor wait requests.
   *
   * @return The collection of information about removed
   *         monitor wait requests
   */
  override def removeAllMonitorWaitRequests(): Seq[MonitorWaitRequestInfo] = {
    monitorWaitRequests.filter(c =>
      monitorWaitManager.removeMonitorWaitRequest(
        c.requestId
      )
    )
  }
}

