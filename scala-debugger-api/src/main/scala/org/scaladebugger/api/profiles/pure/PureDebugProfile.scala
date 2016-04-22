package org.scaladebugger.api.profiles.pure
//import acyclic.file
import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.pure.breakpoints.PureBreakpointProfile
import org.scaladebugger.api.profiles.pure.classes.{PureClassPrepareProfile, PureClassUnloadProfile}
import org.scaladebugger.api.profiles.pure.events.PureEventProfile
import org.scaladebugger.api.profiles.pure.exceptions.PureExceptionProfile
import org.scaladebugger.api.profiles.pure.info.{PureCreateInfoProfile, PureGrabInfoProfile, PureMiscInfoProfile}
import org.scaladebugger.api.profiles.pure.methods.{PureMethodEntryProfile, PureMethodExitProfile}
import org.scaladebugger.api.profiles.pure.monitors.{PureMonitorContendedEnterProfile, PureMonitorContendedEnteredProfile, PureMonitorWaitProfile, PureMonitorWaitedProfile}
import org.scaladebugger.api.profiles.pure.steps.PureStepProfile
import org.scaladebugger.api.profiles.pure.threads.{PureThreadDeathProfile, PureThreadStartProfile}
import org.scaladebugger.api.profiles.pure.vm.{PureVMDeathProfile, PureVMDisconnectProfile, PureVMStartProfile}
import org.scaladebugger.api.profiles.pure.watchpoints.{PureAccessWatchpointProfile, PureModificationWatchpointProfile}
import org.scaladebugger.api.profiles.traits.{DebugProfile, ManagerContainerDebugProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Contains information about the pure debug profile.
 */
object PureDebugProfile {
  val Name: String = "pure"
}

/**
 * Represents a debug profile that adds no extra logic on top of the standard
 * JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine using this profile
 * @param managerContainer The container of low-level managers to use as the
 *                         underlying implementation
 * @param _virtualMachine The underlying virtual machine to use for various
 *                        retrieval methods
 */
class PureDebugProfile(
  protected val scalaVirtualMachine: ScalaVirtualMachine,
  protected val managerContainer: ManagerContainer
)(
  protected val _virtualMachine: VirtualMachine =
    scalaVirtualMachine.underlyingVirtualMachine
) extends ManagerContainerDebugProfile
  with PureAccessWatchpointProfile
  with PureBreakpointProfile
  with PureClassPrepareProfile
  with PureClassUnloadProfile
  with PureCreateInfoProfile
  with PureEventProfile
  with PureExceptionProfile
  with PureGrabInfoProfile
  with PureMethodEntryProfile
  with PureMethodExitProfile
  with PureMiscInfoProfile
  with PureModificationWatchpointProfile
  with PureMonitorContendedEnteredProfile
  with PureMonitorContendedEnterProfile
  with PureMonitorWaitedProfile
  with PureMonitorWaitProfile
  with PureStepProfile
  with PureThreadDeathProfile
  with PureThreadStartProfile
  with PureVMStartProfile
  with PureVMDeathProfile
  with PureVMDisconnectProfile
