package org.scaladebugger.api.profiles.swappable

import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.requests.breakpoints.SwappableBreakpointProfile
import org.scaladebugger.api.profiles.swappable.requests.classes.{SwappableClassPrepareProfile, SwappableClassUnloadProfile}
import org.scaladebugger.api.profiles.swappable.requests.events.SwappableEventListenerProfile
import org.scaladebugger.api.profiles.swappable.requests.exceptions.SwappableExceptionProfile
import org.scaladebugger.api.profiles.swappable.info.{SwappableCreateInfoProfile, SwappableGrabInfoProfile, SwappableMiscInfoProfile}
import org.scaladebugger.api.profiles.swappable.requests.methods.{SwappableMethodEntryProfile, SwappableMethodExitProfile}
import org.scaladebugger.api.profiles.swappable.requests.monitors.{SwappableMonitorContendedEnterProfile, SwappableMonitorContendedEnteredProfile, SwappableMonitorWaitProfile, SwappableMonitorWaitedProfile}
import org.scaladebugger.api.profiles.swappable.requests.steps.SwappableStepProfile
import org.scaladebugger.api.profiles.swappable.requests.threads.{SwappableThreadDeathProfile, SwappableThreadStartProfile}
import org.scaladebugger.api.profiles.swappable.requests.vm.{SwappableVMDeathProfile, SwappableVMDisconnectProfile, SwappableVMStartProfile}
import org.scaladebugger.api.profiles.swappable.requests.watchpoints.{SwappableAccessWatchpointProfile, SwappableModificationWatchpointProfile}
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
  with SwappableEventListenerProfile
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
