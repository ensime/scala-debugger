package org.scaladebugger.api.profiles.pure.requests.vm

import com.sun.jdi.event.VMDeathEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.VMDeathEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.lowlevel.vm._
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.vm.VMDeathProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for vm death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMDeathProfile extends VMDeathProfile {
  protected val vmDeathManager: VMDeathManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newVMDeathRequestHelper()

  /**
   * Constructs a new request helper for vm death.
   *
   * @return The new request helper
   */
  protected def newVMDeathRequestHelper() = {
    type E = VMDeathEvent
    type EI = VMDeathEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = VMDeathEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        vmDeathManager.createVMDeathRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        vmDeathManager.vmDeathRequestList
          .flatMap(vmDeathManager.getVMDeathRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        vmDeathManager.removeVMDeathRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultVMDeathEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = vmDeathManager.getVMDeathRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending vm death requests.
   *
   * @return The collection of information on vm death requests
   */
  override def vmDeathRequests: Seq[VMDeathRequestInfo] = {
    val activeRequests = vmDeathManager.vmDeathRequestList.flatMap(
      vmDeathManager.getVMDeathRequestInfo
    )

    activeRequests ++ (vmDeathManager match {
      case p: PendingVMDeathSupportLike => p.pendingVMDeathRequests
      case _                            => Nil
    })
  }

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateVMDeathRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the vm death request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       vm death request
   * @return True if there is at least one vm death request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isVMDeathRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    vmDeathRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all vm death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       vm death request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeVMDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[VMDeathRequestInfo] = {
    vmDeathRequests.find(_.extraArguments == extraArguments).filter(c =>
      vmDeathManager.removeVMDeathRequest(c.requestId)
    )
  }

  /**
   * Removes all vm death requests.
   *
   * @return The collection of information about removed vm death requests
   */
  override def removeAllVMDeathRequests(): Seq[VMDeathRequestInfo] = {
    vmDeathRequests.filter(c =>
      vmDeathManager.removeVMDeathRequest(c.requestId)
    )
  }
}
