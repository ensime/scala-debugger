package org.scaladebugger.api.profiles.scala210

import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.pure.requests.breakpoints.PureBreakpointRequest
import org.scaladebugger.api.profiles.pure.requests.classes.{PureClassPrepareRequest, PureClassUnloadRequest}
import org.scaladebugger.api.profiles.pure.requests.events.PureEventListenerRequest
import org.scaladebugger.api.profiles.pure.requests.exceptions.PureExceptionRequest
import org.scaladebugger.api.profiles.pure.info.{PureCreateInfoProfile, PureGrabInfoProfile, PureMiscInfo}
import org.scaladebugger.api.profiles.pure.requests.methods.{PureMethodEntryRequest, PureMethodExitRequest}
import org.scaladebugger.api.profiles.pure.requests.monitors.{PureMonitorContendedEnterRequest, PureMonitorContendedEnteredRequest, PureMonitorWaitRequest, PureMonitorWaitedRequest}
import org.scaladebugger.api.profiles.pure.requests.steps.PureStepRequest
import org.scaladebugger.api.profiles.pure.requests.threads.{PureThreadDeathRequest, PureThreadStartRequest}
import org.scaladebugger.api.profiles.pure.requests.vm.{PureVMDeathRequest, PureVMDisconnectRequest, PureVMStartRequest}
import org.scaladebugger.api.profiles.pure.requests.watchpoints.{PureAccessWatchpointRequest, PureModificationWatchpointRequest}
import org.scaladebugger.api.profiles.scala210.info.Scala210InfoProducer
import org.scaladebugger.api.profiles.traits.ManagerContainerDebugProfile
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Contains information about the pure debug profile.
 */
object Scala210DebugProfile {
  val Name: String = "scala-2.10"
}

/**
 * Represents a debug profile that adds Scala 2.10 specific debug logic.
 *
 * @param scalaVirtualMachine The high-level virtual machine using this profile
 * @param managerContainer The container of low-level managers to use as the
 *                         underlying implementation
 * @param _virtualMachine The underlying virtual machine to use for various
 *                        retrieval methods
 * @param infoProducer The producer of information profiles
 */
class Scala210DebugProfile(
  protected val scalaVirtualMachine: ScalaVirtualMachine,
  protected val managerContainer: ManagerContainer
)(
  protected val _virtualMachine: VirtualMachine =
    scalaVirtualMachine.underlyingVirtualMachine,
  protected val infoProducer: InfoProducer =
    new Scala210InfoProducer
) extends ManagerContainerDebugProfile
  with PureAccessWatchpointRequest
  with PureBreakpointRequest
  with PureClassPrepareRequest
  with PureClassUnloadRequest
  with PureCreateInfoProfile
  with PureEventListenerRequest
  with PureExceptionRequest
  with PureGrabInfoProfile
  with PureMethodEntryRequest
  with PureMethodExitRequest
  with PureMiscInfo
  with PureModificationWatchpointRequest
  with PureMonitorContendedEnteredRequest
  with PureMonitorContendedEnterRequest
  with PureMonitorWaitedRequest
  with PureMonitorWaitRequest
  with PureStepRequest
  with PureThreadDeathRequest
  with PureThreadStartRequest
  with PureVMStartRequest
  with PureVMDeathRequest
  with PureVMDisconnectRequest
