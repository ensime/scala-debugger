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
    virtualMachine: => VirtualMachine = locatableEvent.virtualMachine(),
    thread: => ThreadReference = locatableEvent.thread(),
    threadReferenceType: => ReferenceType = locatableEvent.thread().referenceType(),
    location: => Location = locatableEvent.location()
  ): LocatableEventInfoProfile

  def newWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = watchpointEvent.virtualMachine(),
    thread: => ThreadReference = watchpointEvent.thread(),
    threadReferenceType: => ReferenceType = watchpointEvent.thread().referenceType(),
    location: => Location = watchpointEvent.location()
  ): WatchpointEventInfoProfile

  def newAccessWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    container: => Either[ObjectReference, ReferenceType] =
      Option(accessWatchpointEvent.`object`())
        .map(Left.apply)
        .getOrElse(Right(accessWatchpointEvent.field().declaringType())),
    field: => Field = accessWatchpointEvent.field(),
    virtualMachine: => VirtualMachine = accessWatchpointEvent.virtualMachine(),
    thread: => ThreadReference = accessWatchpointEvent.thread(),
    threadReferenceType: => ReferenceType = accessWatchpointEvent.thread().referenceType(),
    location: => Location = accessWatchpointEvent.location()
  ): AccessWatchpointEventInfoProfile

  def newBreakpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = breakpointEvent.virtualMachine(),
    thread: => ThreadReference = breakpointEvent.thread(),
    threadReferenceType: => ReferenceType = breakpointEvent.thread().referenceType(),
    location: => Location = breakpointEvent.location()
  ): BreakpointEventInfoProfile

  def newClassPrepareEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = classPrepareEvent.virtualMachine(),
    thread: => ThreadReference = classPrepareEvent.thread(),
    threadReferenceType: => ReferenceType = classPrepareEvent.thread().referenceType(),
    referenceType: => ReferenceType = classPrepareEvent.referenceType()
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
    catchLocation: => Option[Location] = Option(exceptionEvent.catchLocation()),
    exception: => ObjectReference = exceptionEvent.exception(),
    exceptionReferenceType: => ReferenceType =exceptionEvent.exception().referenceType(),
    virtualMachine: => VirtualMachine = exceptionEvent.virtualMachine(),
    thread: => ThreadReference = exceptionEvent.thread(),
    threadReferenceType: => ReferenceType = exceptionEvent.thread().referenceType(),
    location: => Location = exceptionEvent.location()
  ): ExceptionEventInfoProfile

  def newMethodEntryEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: JDIArgument*
  )(
    method: => Method = methodEntryEvent.method(),
    virtualMachine: => VirtualMachine = methodEntryEvent.virtualMachine(),
    thread: => ThreadReference = methodEntryEvent.thread(),
    threadReferenceType: => ReferenceType = methodEntryEvent.thread().referenceType(),
    location: => Location = methodEntryEvent.location()
  ): MethodEntryEventInfoProfile

  def newMethodExitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: JDIArgument*
  )(
    method: => Method = methodExitEvent.method(),
    returnValue: => Value = methodExitEvent.returnValue(),
    virtualMachine: => VirtualMachine = methodExitEvent.virtualMachine(),
    thread: => ThreadReference = methodExitEvent.thread(),
    threadReferenceType: => ReferenceType = methodExitEvent.thread().referenceType(),
    location: => Location = methodExitEvent.location()
  ): MethodExitEventInfoProfile

  def newModificationWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    container: => Either[ObjectReference, ReferenceType] =
      Option(modificationWatchpointEvent.`object`())
        .map(Left.apply)
        .getOrElse(Right(modificationWatchpointEvent.field().declaringType())),
    field: => Field = modificationWatchpointEvent.field(),
    virtualMachine: => VirtualMachine = modificationWatchpointEvent.virtualMachine(),
    thread: => ThreadReference = modificationWatchpointEvent.thread(),
    threadReferenceType: => ReferenceType = modificationWatchpointEvent.thread().referenceType(),
    location: => Location = modificationWatchpointEvent.location()
  ): ModificationWatchpointEventInfoProfile

  def newMonitorContendedEnteredEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference = monitorContendedEnteredEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorContendedEnteredEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorContendedEnteredEvent.virtualMachine(),
    thread: => ThreadReference = monitorContendedEnteredEvent.thread(),
    threadReferenceType: => ReferenceType = monitorContendedEnteredEvent.thread().referenceType(),
    location: => Location = monitorContendedEnteredEvent.location()
  ): MonitorContendedEnteredEventInfoProfile

  def newMonitorContendedEnterEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference = monitorContendedEnterEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorContendedEnterEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorContendedEnterEvent.virtualMachine(),
    thread: => ThreadReference = monitorContendedEnterEvent.thread(),
    threadReferenceType: => ReferenceType = monitorContendedEnterEvent.thread().referenceType(),
    location: => Location = monitorContendedEnterEvent.location()
  ): MonitorContendedEnterEventInfoProfile

  def newMonitorWaitedEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference = monitorWaitedEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorWaitedEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorWaitedEvent.virtualMachine(),
    thread: => ThreadReference = monitorWaitedEvent.thread(),
    threadReferenceType: => ReferenceType = monitorWaitedEvent.thread().referenceType(),
    location: => Location = monitorWaitedEvent.location()
  ): MonitorWaitedEventInfoProfile

  def newMonitorWaitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: JDIArgument*
  )(
    monitor: => ObjectReference = monitorWaitEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorWaitEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorWaitEvent.virtualMachine(),
    thread: => ThreadReference = monitorWaitEvent.thread(),
    threadReferenceType: => ReferenceType = monitorWaitEvent.thread().referenceType(),
    location: => Location = monitorWaitEvent.location()
  ): MonitorWaitEventInfoProfile

  def newStepEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = stepEvent.virtualMachine(),
    thread: => ThreadReference = stepEvent.thread(),
    threadReferenceType: => ReferenceType = stepEvent.thread().referenceType(),
    location: => Location = stepEvent.location()
  ): StepEventInfoProfile

  def newThreadDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = threadDeathEvent.virtualMachine(),
    thread: => ThreadReference = threadDeathEvent.thread(),
    threadReferenceType: => ReferenceType = threadDeathEvent.thread().referenceType()
  ): ThreadDeathEventInfoProfile

  def newThreadStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine = threadStartEvent.virtualMachine(),
    thread: => ThreadReference = threadStartEvent.thread(),
    threadReferenceType: => ReferenceType = threadStartEvent.thread().referenceType()
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
  )(
    virtualMachine: => VirtualMachine = vmStartEvent.virtualMachine(),
    thread: => ThreadReference = vmStartEvent.thread(),
    threadReferenceType: => ReferenceType = vmStartEvent.thread().referenceType()
  ): VMStartEventInfoProfile
}
