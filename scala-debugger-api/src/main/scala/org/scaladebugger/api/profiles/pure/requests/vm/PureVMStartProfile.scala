package org.scaladebugger.api.profiles.pure.requests.vm

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.event.VMStartEvent
import org.scaladebugger.api.lowlevel.{JDIArgument, StandardRequestInfo}
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.VMStartEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.vm.VMStartProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for vm start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMStartProfile extends VMStartProfile {
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newVMStartRequestHelper()

  /**
   * Constructs a new request helper for vm start.
   *
   * @return The new request helper
   */
  protected def newVMStartRequestHelper() = {
    type E = VMStartEvent
    type EI = VMStartEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    // Used to hold request args across method calls
    import scala.collection.JavaConverters._
    val requestArgsCache = new ConcurrentHashMap[String, RequestArgs]().asScala

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = VMStartEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, _) =>
        Try(requestArgsCache.put(requestId, requestArgs)).map(_ => requestId),
      _hasRequest = id => requestArgsCache.values.toSeq.contains(id),
      _removeRequestById = requestId => requestArgsCache.remove(requestId),
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultVMStartEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = requestId => requestArgsCache.get(requestId)
        .map(rArgs => StandardRequestInfo(requestId, isPending = true, rArgs)),
      _includeUniqueId = false
    )
  }

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateVMStartRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }
}
