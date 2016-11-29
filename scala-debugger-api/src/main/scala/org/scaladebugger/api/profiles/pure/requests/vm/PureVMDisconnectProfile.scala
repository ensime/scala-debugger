package org.scaladebugger.api.profiles.pure.requests.vm
//import acyclic.file

import com.sun.jdi.event.VMDisconnectEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.vm.VMDisconnectProfile
import org.scaladebugger.api.lowlevel.events.EventType.VMDisconnectEventType
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for vm disconnect events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMDisconnectProfile extends VMDisconnectProfile {
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateVMDisconnectRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    eventManager
      .addEventDataStream(VMDisconnectEventType, eArgs: _*)
      .map(t => (t._1.asInstanceOf[VMDisconnectEvent], t._2))
      .map(t => (eventProducer.newDefaultVMDisconnectEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgs: _*
      ), t._2))
      .noop()
  }
}
