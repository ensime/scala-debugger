package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.JavaInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the generic interface used to produce event info instances.
 */
trait EventInfoProducerProfile extends JavaInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: EventInfoProducerProfile

  def newEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: JDIArgument*
  ): EventInfoProfile

  def newLocatableEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): LocatableEventInfoProfile

  def newWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): WatchpointEventInfoProfile

  def newAccessWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    container: => Either[ObjectReference, ReferenceType],
    field: => Field,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): AccessWatchpointEventInfoProfile

  def newBreakpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): BreakpointEventInfoProfile

  def newClassPrepareEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    referenceType: => ReferenceType
  ): ClassPrepareEventInfoProfile

  def newClassUnloadEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: JDIArgument*
  ): ClassUnloadEventInfoProfile

  def newExceptionEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    exceptionEvent: ExceptionEvent,
    jdiArguments: JDIArgument*
  )(
    catchLocation: => Option[Location],
    exception: => ObjectReference,
    exceptionReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): ExceptionEventInfoProfile

  def newMethodEntryEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: JDIArgument*
  )(
    method: => Method,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MethodEntryEventInfoProfile

  def newMethodExitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: JDIArgument*
  )(
    method: => Method,
    returnValue: => Value,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MethodExitEventInfoProfile

  def newModificationWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    container: => Either[ObjectReference, ReferenceType],
    field: => Field,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): ModificationWatchpointEventInfoProfile

  def newMonitorContendedEnteredEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorContendedEnteredEventInfoProfile

  def newMonitorContendedEnterEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorContendedEnterEventInfoProfile

  def newMonitorWaitedEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorWaitedEventInfoProfile

  def newMonitorWaitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorWaitEventInfoProfile

  def newStepEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): StepEventInfoProfile

  def newThreadDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadDeathEventInfoProfile

  def newThreadStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadStartEventInfoProfile

  def newVMDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: JDIArgument*
  ): VMDeathEventInfoProfile

  def newVMDisconnectEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: JDIArgument*
  ): VMDisconnectEventInfoProfile

  def newVMStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: JDIArgument*
  ): VMStartEventInfoProfile
}
