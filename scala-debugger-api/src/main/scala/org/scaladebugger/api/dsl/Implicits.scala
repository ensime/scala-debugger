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
import org.scaladebugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.classes.{ClassPrepareProfile, ClassUnloadProfile}
import org.scaladebugger.api.profiles.traits.events.EventListenerProfile
import org.scaladebugger.api.profiles.traits.exceptions.ExceptionProfile
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.methods.{MethodEntryProfile, MethodExitProfile}
import org.scaladebugger.api.profiles.traits.monitors.{MonitorContendedEnterProfile, MonitorContendedEnteredProfile, MonitorWaitProfile, MonitorWaitedProfile}
import org.scaladebugger.api.profiles.traits.steps.StepProfile
import org.scaladebugger.api.profiles.traits.threads.{ThreadDeathProfile, ThreadStartProfile}
import org.scaladebugger.api.profiles.traits.vm.{VMDeathProfile, VMDisconnectProfile, VMStartProfile}
import org.scaladebugger.api.profiles.traits.watchpoints.{AccessWatchpointProfile, ModificationWatchpointProfile}

/**
 * Contains implicit classes to provide DSL-like methods to the debugger API.
 */
object Implicits {
  import scala.language.implicitConversions

  /** Converts breakpoint profile to implicit DSL wrapping. */
  implicit def BreakpointDSL(
    breakpointProfile: BreakpointProfile
  ): BreakpointDSLWrapper = new BreakpointDSLWrapper(breakpointProfile)

  /** Converts class prepare profile to implicit DSL wrapping. */
  implicit def ClassPrepareDSL(
    classPrepareProfile: ClassPrepareProfile
  ): ClassPrepareDSLWrapper = new ClassPrepareDSLWrapper(classPrepareProfile)

  /** Converts class unload profile to implicit DSL wrapping. */
  implicit def ClassUnloadDSL(
    classUnloadProfile: ClassUnloadProfile
  ): ClassUnloadDSLWrapper = new ClassUnloadDSLWrapper(classUnloadProfile)

  /** Converts event listener profile to implicit DSL wrapping. */
  implicit def EventListenerDSL(
    eventListenerProfile: EventListenerProfile
  ): EventListenerDSLWrapper = new EventListenerDSLWrapper(eventListenerProfile)

  /** Converts exception profile to implicit DSL wrapping. */
  implicit def ExceptionDSL(
    exceptionProfile: ExceptionProfile
  ): ExceptionDSLWrapper = new ExceptionDSLWrapper(exceptionProfile)

  /** Converts frame info profile to implicit DSL wrapping. */
  implicit def FrameInfoDSL(
    frameInfoProfile: FrameInfoProfile
  ): FrameInfoDSLWrapper = new FrameInfoDSLWrapper(frameInfoProfile)

  /** Converts grab info profile to implicit DSL wrapping. */
  implicit def GrabInfoDSL(
    grabInfoProfile: GrabInfoProfile
  ): GrabInfoDSLWrapper = new GrabInfoDSLWrapper(grabInfoProfile)

  /** Converts method entry profile to implicit DSL wrapping. */
  implicit def MethodEntryDSL(
    methodEntryProfile: MethodEntryProfile
  ): MethodEntryDSLWrapper = new MethodEntryDSLWrapper(methodEntryProfile)

  /** Converts method exit profile to implicit DSL wrapping. */
  implicit def MethodExitDSL(
    methodExitProfile: MethodExitProfile
  ): MethodExitDSLWrapper = new MethodExitDSLWrapper(methodExitProfile)

  /** Converts monitor contended entered profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnteredDSL(
    monitorContendedEnteredProfile: MonitorContendedEnteredProfile
  ): MonitorContendedEnteredDSLWrapper = new MonitorContendedEnteredDSLWrapper(monitorContendedEnteredProfile)

  /** Converts monitor contended enter profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnterDSL(
    monitorContendedEnterProfile: MonitorContendedEnterProfile
  ): MonitorContendedEnterDSLWrapper = new MonitorContendedEnterDSLWrapper(monitorContendedEnterProfile)

  /** Converts monitor waited profile to implicit DSL wrapping. */
  implicit def MonitorWaitedDSL(
    monitorWaitedProfile: MonitorWaitedProfile
  ): MonitorWaitedDSLWrapper = new MonitorWaitedDSLWrapper(monitorWaitedProfile)

  /** Converts monitor wait profile to implicit DSL wrapping. */
  implicit def MonitorWaitDSL(
    monitorWaitProfile: MonitorWaitProfile
  ): MonitorWaitDSLWrapper = new MonitorWaitDSLWrapper(monitorWaitProfile)

  /** Converts object info profile to implicit DSL wrapping. */
  implicit def ObjectInfoDSL[T <: ObjectInfoProfile](
    objectInfoProfile: T
  ): ObjectInfoDSLWrapper[T] = new ObjectInfoDSLWrapper(objectInfoProfile)

  /** Converts step profile to implicit DSL wrapping. */
  implicit def StepDSL(
    stepProfile: StepProfile
  ): StepDSLWrapper = new StepDSLWrapper(stepProfile)

  /** Converts thread death profile to implicit DSL wrapping. */
  implicit def ThreadDeathDSL(
    threadDeathProfile: ThreadDeathProfile
  ): ThreadDeathDSLWrapper = new ThreadDeathDSLWrapper(threadDeathProfile)

  /** Converts thread start profile to implicit DSL wrapping. */
  implicit def ThreadStartDSL(
    threadStartProfile: ThreadStartProfile
  ): ThreadStartDSLWrapper = new ThreadStartDSLWrapper(threadStartProfile)

  /** Converts value info profile to implicit DSL wrapping. */
  implicit def ValueInfoDSL[T <: ValueInfoProfile](
    valueInfoProfile: T
  ): ValueInfoDSLWrapper[T] = new ValueInfoDSLWrapper(valueInfoProfile)

  /** Converts variable info profile to implicit DSL wrapping. */
  implicit def VariableInfoDSL(
    variableInfoProfile: VariableInfoProfile
  ): VariableInfoDSLWrapper = new VariableInfoDSLWrapper(variableInfoProfile)

  /** Converts vm death profile to implicit DSL wrapping. */
  implicit def VMDeathDSL(
    vmDeathProfile: VMDeathProfile
  ): VMDeathDSLWrapper = new VMDeathDSLWrapper(vmDeathProfile)

  /** Converts vm disconnect profile to implicit DSL wrapping. */
  implicit def VMDisconnectDSL(
    vmDisconnectProfile: VMDisconnectProfile
  ): VMDisconnectDSLWrapper = new VMDisconnectDSLWrapper(vmDisconnectProfile)

  /** Converts vm start profile to implicit DSL wrapping. */
  implicit def VMStartDSL(
    vmStartProfile: VMStartProfile
  ): VMStartDSLWrapper = new VMStartDSLWrapper(vmStartProfile)

  /** Converts access watchpoint profile to implicit DSL wrapping. */
  implicit def AccessWatchpointDSL(
    accessWatchpointProfile: AccessWatchpointProfile
  ): AccessWatchpointDSLWrapper = new AccessWatchpointDSLWrapper(accessWatchpointProfile)

  /** Converts modification watchpoint profile to implicit DSL wrapping. */
  implicit def ModificationWatchpointDSL(
    modificationWatchpointProfile: ModificationWatchpointProfile
  ): ModificationWatchpointDSLWrapper = new ModificationWatchpointDSLWrapper(modificationWatchpointProfile)
}
