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
    breakpointRequest: BreakpointRequest
  ): BreakpointDSLWrapper = new BreakpointDSLWrapper(breakpointRequest)

  /** Converts class prepare profile to implicit DSL wrapping. */
  implicit def ClassPrepareDSL(
    classPrepareRequest: ClassPrepareRequest
  ): ClassPrepareDSLWrapper = new ClassPrepareDSLWrapper(classPrepareRequest)

  /** Converts class unload profile to implicit DSL wrapping. */
  implicit def ClassUnloadDSL(
    classUnloadRequest: ClassUnloadRequest
  ): ClassUnloadDSLWrapper = new ClassUnloadDSLWrapper(classUnloadRequest)

  /** Converts event listener profile to implicit DSL wrapping. */
  implicit def EventListenerDSL(
    eventListenerRequest: EventListenerRequest
  ): EventListenerDSLWrapper = new EventListenerDSLWrapper(eventListenerRequest)

  /** Converts exception profile to implicit DSL wrapping. */
  implicit def ExceptionDSL(
    exceptionRequest: ExceptionRequest
  ): ExceptionDSLWrapper = new ExceptionDSLWrapper(exceptionRequest)

  /** Converts frame info profile to implicit DSL wrapping. */
  implicit def FrameInfoDSL(
    frameInfoRequest: FrameInfo
  ): FrameInfoDSLWrapper = new FrameInfoDSLWrapper(frameInfoRequest)

  /** Converts grab info profile to implicit DSL wrapping. */
  implicit def GrabInfoDSL(
    grabInfoProfile: GrabInfoProfile
  ): GrabInfoDSLWrapper = new GrabInfoDSLWrapper(grabInfoProfile)

  /** Converts method entry profile to implicit DSL wrapping. */
  implicit def MethodEntryDSL(
    methodEntryRequest: MethodEntryRequest
  ): MethodEntryDSLWrapper = new MethodEntryDSLWrapper(methodEntryRequest)

  /** Converts method exit profile to implicit DSL wrapping. */
  implicit def MethodExitDSL(
    methodExitRequest: MethodExitRequest
  ): MethodExitDSLWrapper = new MethodExitDSLWrapper(methodExitRequest)

  /** Converts monitor contended entered profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnteredDSL(
    monitorContendedEnteredRequest: MonitorContendedEnteredRequest
  ): MonitorContendedEnteredDSLWrapper = new MonitorContendedEnteredDSLWrapper(monitorContendedEnteredRequest)

  /** Converts monitor contended enter profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnterDSL(
    monitorContendedEnterRequest: MonitorContendedEnterRequest
  ): MonitorContendedEnterDSLWrapper = new MonitorContendedEnterDSLWrapper(monitorContendedEnterRequest)

  /** Converts monitor waited profile to implicit DSL wrapping. */
  implicit def MonitorWaitedDSL(
    monitorWaitedRequest: MonitorWaitedRequest
  ): MonitorWaitedDSLWrapper = new MonitorWaitedDSLWrapper(monitorWaitedRequest)

  /** Converts monitor wait profile to implicit DSL wrapping. */
  implicit def MonitorWaitDSL(
    monitorWaitRequest: MonitorWaitRequest
  ): MonitorWaitDSLWrapper = new MonitorWaitDSLWrapper(monitorWaitRequest)

  /** Converts object info profile to implicit DSL wrapping. */
  implicit def ObjectInfoDSL[T <: ObjectInfo](
    objectInfoRequest: T
  ): ObjectInfoDSLWrapper[T] = new ObjectInfoDSLWrapper(objectInfoRequest)

  /** Converts step profile to implicit DSL wrapping. */
  implicit def StepDSL(
    stepRequest: StepRequest
  ): StepDSLWrapper = new StepDSLWrapper(stepRequest)

  /** Converts thread death profile to implicit DSL wrapping. */
  implicit def ThreadDeathDSL(
    threadDeathRequest: ThreadDeathRequest
  ): ThreadDeathDSLWrapper = new ThreadDeathDSLWrapper(threadDeathRequest)

  /** Converts thread start profile to implicit DSL wrapping. */
  implicit def ThreadStartDSL(
    threadStartRequest: ThreadStartRequest
  ): ThreadStartDSLWrapper = new ThreadStartDSLWrapper(threadStartRequest)

  /** Converts value info profile to implicit DSL wrapping. */
  implicit def ValueInfoDSL[T <: ValueInfo](
    valueInfoRequest: T
  ): ValueInfoDSLWrapper[T] = new ValueInfoDSLWrapper(valueInfoRequest)

  /** Converts variable info profile to implicit DSL wrapping. */
  implicit def VariableInfoDSL(
    variableInfoRequest: VariableInfo
  ): VariableInfoDSLWrapper = new VariableInfoDSLWrapper(variableInfoRequest)

  /** Converts vm death profile to implicit DSL wrapping. */
  implicit def VMDeathDSL(
    vmDeathRequest: VMDeathRequest
  ): VMDeathDSLWrapper = new VMDeathDSLWrapper(vmDeathRequest)

  /** Converts vm disconnect profile to implicit DSL wrapping. */
  implicit def VMDisconnectDSL(
    vmDisconnectRequest: VMDisconnectRequest
  ): VMDisconnectDSLWrapper = new VMDisconnectDSLWrapper(vmDisconnectRequest)

  /** Converts vm start profile to implicit DSL wrapping. */
  implicit def VMStartDSL(
    vmStartRequest: VMStartRequest
  ): VMStartDSLWrapper = new VMStartDSLWrapper(vmStartRequest)

  /** Converts access watchpoint profile to implicit DSL wrapping. */
  implicit def AccessWatchpointDSL(
    accessWatchpointRequest: AccessWatchpointRequest
  ): AccessWatchpointDSLWrapper = new AccessWatchpointDSLWrapper(accessWatchpointRequest)

  /** Converts modification watchpoint profile to implicit DSL wrapping. */
  implicit def ModificationWatchpointDSL(
    modificationWatchpointRequest: ModificationWatchpointRequest
  ): ModificationWatchpointDSLWrapper = new ModificationWatchpointDSLWrapper(modificationWatchpointRequest)
}
