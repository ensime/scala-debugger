package org.scaladebugger.api.profiles.traits
//import acyclic.file

import org.scaladebugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.classes.{ClassPrepareProfile, ClassUnloadProfile}
import org.scaladebugger.api.profiles.traits.events.EventProfile
import org.scaladebugger.api.profiles.traits.exceptions.ExceptionProfile
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, GrabInfoProfile, MiscInfoProfile}
import org.scaladebugger.api.profiles.traits.methods.{MethodEntryProfile, MethodExitProfile}
import org.scaladebugger.api.profiles.traits.monitors.{MonitorContendedEnterProfile, MonitorContendedEnteredProfile, MonitorWaitProfile, MonitorWaitedProfile}
import org.scaladebugger.api.profiles.traits.steps.StepProfile
import org.scaladebugger.api.profiles.traits.threads.{ThreadDeathProfile, ThreadStartProfile}
import org.scaladebugger.api.profiles.traits.vm.{VMDeathProfile, VMDisconnectProfile, VMStartProfile}
import org.scaladebugger.api.profiles.traits.watchpoints.{AccessWatchpointProfile, ModificationWatchpointProfile}

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
  with EventProfile
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
