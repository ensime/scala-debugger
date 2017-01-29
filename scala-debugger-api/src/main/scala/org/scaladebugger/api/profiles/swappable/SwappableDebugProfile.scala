package org.scaladebugger.api.profiles.swappable

import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.requests.breakpoints.SwappableBreakpointRequest
import org.scaladebugger.api.profiles.swappable.requests.classes.{SwappableClassPrepareRequest, SwappableClassUnloadRequest}
import org.scaladebugger.api.profiles.swappable.requests.events.SwappableEventListenerRequest
import org.scaladebugger.api.profiles.swappable.requests.exceptions.SwappableExceptionRequest
import org.scaladebugger.api.profiles.swappable.info.{SwappableCreateInfo, SwappableGrabInfoProfile, SwappableMiscInfo}
import org.scaladebugger.api.profiles.swappable.requests.methods.{SwappableMethodEntryRequest, SwappableMethodExitRequest}
import org.scaladebugger.api.profiles.swappable.requests.monitors.{SwappableMonitorContendedEnterRequest, SwappableMonitorContendedEnteredRequest, SwappableMonitorWaitRequest, SwappableMonitorWaitedRequest}
import org.scaladebugger.api.profiles.swappable.requests.steps.SwappableStepRequest
import org.scaladebugger.api.profiles.swappable.requests.threads.{SwappableThreadDeathRequest, SwappableThreadStartRequest}
import org.scaladebugger.api.profiles.swappable.requests.vm.{SwappableVMDeathRequest, SwappableVMDisconnectRequest, SwappableVMStartRequest}
import org.scaladebugger.api.profiles.swappable.requests.watchpoints.{SwappableAccessWatchpointRequest, SwappableModificationWatchpointRequest}
import org.scaladebugger.api.profiles.traits.DebugProfile

/**
 * Contains information about the java debug profile.
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
  with SwappableAccessWatchpointRequest
  with SwappableBreakpointRequest
  with SwappableClassPrepareRequest
  with SwappableClassUnloadRequest
  with SwappableCreateInfo
  with SwappableEventListenerRequest
  with SwappableExceptionRequest
  with SwappableGrabInfoProfile
  with SwappableMethodEntryRequest
  with SwappableMethodExitRequest
  with SwappableMiscInfo
  with SwappableModificationWatchpointRequest
  with SwappableMonitorContendedEnteredRequest
  with SwappableMonitorContendedEnterRequest
  with SwappableMonitorWaitedRequest
  with SwappableMonitorWaitRequest
  with SwappableStepRequest
  with SwappableThreadDeathRequest
  with SwappableThreadStartRequest
  with SwappableVMStartRequest
  with SwappableVMDeathRequest
  with SwappableVMDisconnectRequest
