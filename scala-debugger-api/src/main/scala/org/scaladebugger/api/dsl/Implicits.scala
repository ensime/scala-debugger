package org.scaladebugger.api.dsl

import org.scaladebugger.api.dsl.breakpoints.BreakpointDSLWrapper
import org.scaladebugger.api.dsl.classes.{ClassPrepareDSLWrapper, ClassUnloadDSLWrapper}
import org.scaladebugger.api.dsl.events.EventListenerDSLWrapper
import org.scaladebugger.api.dsl.exceptions.ExceptionDSLWrapper
import org.scaladebugger.api.dsl.info._
import org.scaladebugger.api.dsl.methods.{MethodEntryDSLWrapper, MethodExitDSLWrapper}
import org.scaladebugger.api.dsl.monitors.{MonitorContendedEnterDSLWrapper, MonitorContendedEnteredDSLWrapper, MonitorWaitDSLWrapper, MonitorWaitedDSLWrapper}
import org.scaladebugger.api.dsl.steps.StepDSLWrapper
import org.scaladebugger.api.dsl.threads.{ThreadDeathDSLWrapper, ThreadStartDSLWrapper}
import org.scaladebugger.api.dsl.vm.{VMDeathDSLWrapper, VMDisconnectDSLWrapper, VMStartDSLWrapper}
import org.scaladebugger.api.dsl.watchpoints.{AccessWatchpointDSLWrapper, ModificationWatchpointDSLWrapper}
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointRequest
import org.scaladebugger.api.profiles.traits.requests.classes.{ClassPrepareRequest, ClassUnloadRequest}
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerRequest
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionRequest
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.requests.methods.{MethodEntryRequest, MethodExitRequest}
import org.scaladebugger.api.profiles.traits.requests.monitors.{MonitorContendedEnterRequest, MonitorContendedEnteredRequest, MonitorWaitRequest, MonitorWaitedRequest}
import org.scaladebugger.api.profiles.traits.requests.steps.StepRequest
import org.scaladebugger.api.profiles.traits.requests.threads.{ThreadDeathRequest, ThreadStartRequest}
import org.scaladebugger.api.profiles.traits.requests.vm.{VMDeathRequest, VMDisconnectRequest, VMStartRequest}
import org.scaladebugger.api.profiles.traits.requests.watchpoints.{AccessWatchpointRequest, ModificationWatchpointRequest}

/**
 * Contains implicit classes to provide DSL-like methods to the debugger API.
 */
object Implicits {
  import scala.language.implicitConversions

  /** Converts breakpoint profile to implicit DSL wrapping. */
  implicit def BreakpointDSL(
    breakpointProfile: BreakpointRequest
  ): BreakpointDSLWrapper = new BreakpointDSLWrapper(breakpointProfile)

  /** Converts class prepare profile to implicit DSL wrapping. */
  implicit def ClassPrepareDSL(
    classPrepareProfile: ClassPrepareRequest
  ): ClassPrepareDSLWrapper = new ClassPrepareDSLWrapper(classPrepareProfile)

  /** Converts class unload profile to implicit DSL wrapping. */
  implicit def ClassUnloadDSL(
    classUnloadProfile: ClassUnloadRequest
  ): ClassUnloadDSLWrapper = new ClassUnloadDSLWrapper(classUnloadProfile)

  /** Converts event listener profile to implicit DSL wrapping. */
  implicit def EventListenerDSL(
    eventListenerProfile: EventListenerRequest
  ): EventListenerDSLWrapper = new EventListenerDSLWrapper(eventListenerProfile)

  /** Converts exception profile to implicit DSL wrapping. */
  implicit def ExceptionDSL(
    exceptionProfile: ExceptionRequest
  ): ExceptionDSLWrapper = new ExceptionDSLWrapper(exceptionProfile)

  /** Converts frame info profile to implicit DSL wrapping. */
  implicit def FrameInfoDSL(
    frameInfoProfile: FrameInfo
  ): FrameInfoDSLWrapper = new FrameInfoDSLWrapper(frameInfoProfile)

  /** Converts grab info profile to implicit DSL wrapping. */
  implicit def GrabInfoDSL(
    grabInfoProfile: GrabInfoProfile
  ): GrabInfoDSLWrapper = new GrabInfoDSLWrapper(grabInfoProfile)

  /** Converts method entry profile to implicit DSL wrapping. */
  implicit def MethodEntryDSL(
    methodEntryProfile: MethodEntryRequest
  ): MethodEntryDSLWrapper = new MethodEntryDSLWrapper(methodEntryProfile)

  /** Converts method exit profile to implicit DSL wrapping. */
  implicit def MethodExitDSL(
    methodExitProfile: MethodExitRequest
  ): MethodExitDSLWrapper = new MethodExitDSLWrapper(methodExitProfile)

  /** Converts monitor contended entered profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnteredDSL(
    monitorContendedEnteredProfile: MonitorContendedEnteredRequest
  ): MonitorContendedEnteredDSLWrapper = new MonitorContendedEnteredDSLWrapper(monitorContendedEnteredProfile)

  /** Converts monitor contended enter profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnterDSL(
    monitorContendedEnterProfile: MonitorContendedEnterRequest
  ): MonitorContendedEnterDSLWrapper = new MonitorContendedEnterDSLWrapper(monitorContendedEnterProfile)

  /** Converts monitor waited profile to implicit DSL wrapping. */
  implicit def MonitorWaitedDSL(
    monitorWaitedProfile: MonitorWaitedRequest
  ): MonitorWaitedDSLWrapper = new MonitorWaitedDSLWrapper(monitorWaitedProfile)

  /** Converts monitor wait profile to implicit DSL wrapping. */
  implicit def MonitorWaitDSL(
    monitorWaitProfile: MonitorWaitRequest
  ): MonitorWaitDSLWrapper = new MonitorWaitDSLWrapper(monitorWaitProfile)

  /** Converts object info profile to implicit DSL wrapping. */
  implicit def ObjectInfoDSL[T <: ObjectInfo](
    objectInfoProfile: T
  ): ObjectInfoDSLWrapper[T] = new ObjectInfoDSLWrapper(objectInfoProfile)

  /** Converts step profile to implicit DSL wrapping. */
  implicit def StepDSL(
    stepProfile: StepRequest
  ): StepDSLWrapper = new StepDSLWrapper(stepProfile)

  /** Converts thread death profile to implicit DSL wrapping. */
  implicit def ThreadDeathDSL(
    threadDeathProfile: ThreadDeathRequest
  ): ThreadDeathDSLWrapper = new ThreadDeathDSLWrapper(threadDeathProfile)

  /** Converts thread start profile to implicit DSL wrapping. */
  implicit def ThreadStartDSL(
    threadStartProfile: ThreadStartRequest
  ): ThreadStartDSLWrapper = new ThreadStartDSLWrapper(threadStartProfile)

  /** Converts value info profile to implicit DSL wrapping. */
  implicit def ValueInfoDSL[T <: ValueInfo](
    valueInfoProfile: T
  ): ValueInfoDSLWrapper[T] = new ValueInfoDSLWrapper(valueInfoProfile)

  /** Converts variable info profile to implicit DSL wrapping. */
  implicit def VariableInfoDSL(
    variableInfoProfile: VariableInfo
  ): VariableInfoDSLWrapper = new VariableInfoDSLWrapper(variableInfoProfile)

  /** Converts vm death profile to implicit DSL wrapping. */
  implicit def VMDeathDSL(
    vmDeathProfile: VMDeathRequest
  ): VMDeathDSLWrapper = new VMDeathDSLWrapper(vmDeathProfile)

  /** Converts vm disconnect profile to implicit DSL wrapping. */
  implicit def VMDisconnectDSL(
    vmDisconnectProfile: VMDisconnectRequest
  ): VMDisconnectDSLWrapper = new VMDisconnectDSLWrapper(vmDisconnectProfile)

  /** Converts vm start profile to implicit DSL wrapping. */
  implicit def VMStartDSL(
    vmStartProfile: VMStartRequest
  ): VMStartDSLWrapper = new VMStartDSLWrapper(vmStartProfile)

  /** Converts access watchpoint profile to implicit DSL wrapping. */
  implicit def AccessWatchpointDSL(
    accessWatchpointProfile: AccessWatchpointRequest
  ): AccessWatchpointDSLWrapper = new AccessWatchpointDSLWrapper(accessWatchpointProfile)

  /** Converts modification watchpoint profile to implicit DSL wrapping. */
  implicit def ModificationWatchpointDSL(
    modificationWatchpointProfile: ModificationWatchpointRequest
  ): ModificationWatchpointDSLWrapper = new ModificationWatchpointDSLWrapper(modificationWatchpointProfile)
}
