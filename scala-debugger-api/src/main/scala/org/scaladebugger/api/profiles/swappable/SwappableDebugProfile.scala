package org.scaladebugger.api.profiles.swappable
//import acyclic.file

import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.breakpoints.SwappableBreakpointProfile
import org.scaladebugger.api.profiles.swappable.classes.{SwappableClassPrepareProfile, SwappableClassUnloadProfile}
import org.scaladebugger.api.profiles.swappable.events.SwappableEventProfile
import org.scaladebugger.api.profiles.swappable.exceptions.SwappableExceptionProfile
import org.scaladebugger.api.profiles.swappable.info.{SwappableCreateInfoProfile, SwappableGrabInfoProfile, SwappableMiscInfoProfile}
import org.scaladebugger.api.profiles.swappable.methods.{SwappableMethodEntryProfile, SwappableMethodExitProfile}
import org.scaladebugger.api.profiles.swappable.monitors.{SwappableMonitorContendedEnterProfile, SwappableMonitorContendedEnteredProfile, SwappableMonitorWaitProfile, SwappableMonitorWaitedProfile}
import org.scaladebugger.api.profiles.swappable.steps.SwappableStepProfile
import org.scaladebugger.api.profiles.swappable.threads.{SwappableThreadDeathProfile, SwappableThreadStartProfile}
import org.scaladebugger.api.profiles.swappable.vm.{SwappableVMDeathProfile, SwappableVMDisconnectProfile, SwappableVMStartProfile}
import org.scaladebugger.api.profiles.swappable.watchpoints.{SwappableAccessWatchpointProfile, SwappableModificationWatchpointProfile}
import org.scaladebugger.api.profiles.traits.DebugProfile

/**
 * Contains information about the pure debug profile.
 */
object SwappableDebugProfile {
  val Name: String = "swappable"
}

/**
 * Represents a debug profile that allows swapping the actual profile
 * implementation underneath.
 */
trait SwappableDebugProfile
  extends DebugProfile
  with SwappableDebugProfileManagement
  with SwappableAccessWatchpointProfile
  with SwappableBreakpointProfile
  with SwappableClassPrepareProfile
  with SwappableClassUnloadProfile
  with SwappableCreateInfoProfile
  with SwappableEventProfile
  with SwappableExceptionProfile
  with SwappableGrabInfoProfile
  with SwappableMethodEntryProfile
  with SwappableMethodExitProfile
  with SwappableMiscInfoProfile
  with SwappableModificationWatchpointProfile
  with SwappableMonitorContendedEnteredProfile
  with SwappableMonitorContendedEnterProfile
  with SwappableMonitorWaitedProfile
  with SwappableMonitorWaitProfile
  with SwappableStepProfile
  with SwappableThreadDeathProfile
  with SwappableThreadStartProfile
  with SwappableVMStartProfile
  with SwappableVMDeathProfile
  with SwappableVMDisconnectProfile
