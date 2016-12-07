package org.scaladebugger.api.profiles.traits

import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointRequest
import org.scaladebugger.api.profiles.traits.requests.classes.{ClassPrepareRequest, ClassUnloadRequest}
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerRequest
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionRequest
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, GrabInfoProfile, MiscInfo}
import org.scaladebugger.api.profiles.traits.requests.methods.{MethodEntryRequest, MethodExitRequest}
import org.scaladebugger.api.profiles.traits.requests.monitors.{MonitorContendedEnterRequest, MonitorContendedEnteredRequest, MonitorWaitRequest, MonitorWaitedRequest}
import org.scaladebugger.api.profiles.traits.requests.steps.StepRequest
import org.scaladebugger.api.profiles.traits.requests.threads.{ThreadDeathRequest, ThreadStartRequest}
import org.scaladebugger.api.profiles.traits.requests.vm.{VMDeathRequest, VMDisconnectRequest, VMStartRequest}
import org.scaladebugger.api.profiles.traits.requests.watchpoints.{AccessWatchpointRequest, ModificationWatchpointRequest}

/**
 * Represents the interface that needs to be implemented to provide
 * functionality for a specific debug profile.
 */
trait DebugProfile
  extends AccessWatchpointRequest
  with BreakpointRequest
  with ClassPrepareRequest
  with ClassUnloadRequest
  with CreateInfoProfile
  with EventListenerRequest
  with ExceptionRequest
  with GrabInfoProfile
  with MethodEntryRequest
  with MethodExitRequest
  with MiscInfo
  with ModificationWatchpointRequest
  with MonitorContendedEnteredRequest
  with MonitorContendedEnterRequest
  with MonitorWaitedRequest
  with MonitorWaitRequest
  with StepRequest
  with ThreadDeathRequest
  with ThreadStartRequest
  with VMStartRequest
  with VMDeathRequest
  with VMDisconnectRequest
