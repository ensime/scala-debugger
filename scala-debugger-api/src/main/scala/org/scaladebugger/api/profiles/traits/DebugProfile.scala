package org.scaladebugger.api.profiles.traits

import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.requests.classes.{ClassPrepareProfile, ClassUnloadProfile}
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerProfile
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionProfile
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, GrabInfoProfile, MiscInfoProfile}
import org.scaladebugger.api.profiles.traits.requests.methods.{MethodEntryProfile, MethodExitProfile}
import org.scaladebugger.api.profiles.traits.requests.monitors.{MonitorContendedEnterProfile, MonitorContendedEnteredProfile, MonitorWaitProfile, MonitorWaitedProfile}
import org.scaladebugger.api.profiles.traits.requests.steps.StepProfile
import org.scaladebugger.api.profiles.traits.requests.threads.{ThreadDeathProfile, ThreadStartProfile}
import org.scaladebugger.api.profiles.traits.requests.vm.{VMDeathProfile, VMDisconnectProfile, VMStartProfile}
import org.scaladebugger.api.profiles.traits.requests.watchpoints.{AccessWatchpointProfile, ModificationWatchpointProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * functionality for a specific debug profile.
 */
trait DebugProfile
  extends AccessWatchpointProfile
  with BreakpointProfile
  with ClassPrepareProfile
  with ClassUnloadProfile
  with CreateInfoProfile
  with EventListenerProfile
  with ExceptionProfile
  with GrabInfoProfile
  with MethodEntryProfile
  with MethodExitProfile
  with MiscInfoProfile
  with ModificationWatchpointProfile
  with MonitorContendedEnteredProfile
  with MonitorContendedEnterProfile
  with MonitorWaitedProfile
  with MonitorWaitProfile
  with StepProfile
  with ThreadDeathProfile
  with ThreadStartProfile
  with VMStartProfile
  with VMDeathProfile
  with VMDisconnectProfile
