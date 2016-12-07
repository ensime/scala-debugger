package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, JavaInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the generic interface used to produce event info instances.
 */
trait EventInfoProducer extends JavaInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: EventInfoProducer

  /**
   * Returns the parent information producer of this event information
   * producer.
   *
   * @return The parent information producer
   */
  def infoProducer: InfoProducer

  /** Fills in additional properties with default values. */
  def newDefaultEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: JDIArgument*
  ): EventInfo = newEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    event = event,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the event profile. */
  def newEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: Seq[JDIArgument]
  ): EventInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocatableEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: JDIArgument*
  ): LocatableEventInfo = newLocatableEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    locatableEvent = locatableEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the locatable event profile. */
  def newLocatableEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = locatableEvent.virtualMachine(),
    thread: => ThreadReference = locatableEvent.thread(),
    threadReferenceType: => ReferenceType = locatableEvent.thread().referenceType(),
    location: => Location = locatableEvent.location()
  ): LocatableEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMonitorEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorEvent: MonitorEvent,
    jdiArguments: JDIArgument*
  ): MonitorEventInfo = newMonitorEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorEvent = monitorEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended entered event profile. */
  def newMonitorEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorEvent: MonitorEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference = monitorEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorEvent.virtualMachine(),
    thread: => ThreadReference = monitorEvent.thread(),
    threadReferenceType: => ReferenceType = monitorEvent.thread().referenceType(),
    location: => Location = monitorEvent.location()
  ): MonitorEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: JDIArgument*
  ): WatchpointEventInfo = newWatchpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    watchpointEvent = watchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the watchpoint event profile. */
  def newWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    container: => Either[ObjectReference, ReferenceType] =
      Option(watchpointEvent.`object`())
        .map(Left.apply)
        .getOrElse(Right(watchpointEvent.field().declaringType())),
    field: => Field = watchpointEvent.field(),
    virtualMachine: => VirtualMachine = watchpointEvent.virtualMachine(),
    thread: => ThreadReference = watchpointEvent.thread(),
    threadReferenceType: => ReferenceType = watchpointEvent.thread().referenceType(),
    location: => Location = watchpointEvent.location()
  ): WatchpointEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultAccessWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: JDIArgument*
  ): AccessWatchpointEventInfo = newAccessWatchpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    accessWatchpointEvent = accessWatchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the access watchpoint event profile. */
  def newAccessWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: Seq[JDIArgument]
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
  ): AccessWatchpointEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultBreakpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: JDIArgument*
  ): BreakpointEventInfo = newBreakpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    breakpointEvent = breakpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the breakpoint event profile. */
  def newBreakpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = breakpointEvent.virtualMachine(),
    thread: => ThreadReference = breakpointEvent.thread(),
    threadReferenceType: => ReferenceType = breakpointEvent.thread().referenceType(),
    location: => Location = breakpointEvent.location()
  ): BreakpointEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassPrepareEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: JDIArgument*
  ): ClassPrepareEventInfo = newClassPrepareEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classPrepareEvent = classPrepareEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the class prepare event profile. */
  def newClassPrepareEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = classPrepareEvent.virtualMachine(),
    thread: => ThreadReference = classPrepareEvent.thread(),
    threadReferenceType: => ReferenceType = classPrepareEvent.thread().referenceType(),
    referenceType: => ReferenceType = classPrepareEvent.referenceType()
  ): ClassPrepareEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultClassUnloadEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: JDIArgument*
  ): ClassUnloadEventInfo = newClassUnloadEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    classUnloadEvent = classUnloadEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the class unload event profile. */
  def newClassUnloadEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: Seq[JDIArgument]
  ): ClassUnloadEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultExceptionEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    exceptionEvent: ExceptionEvent,
    jdiArguments: JDIArgument*
  ): ExceptionEventInfo = newExceptionEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    exceptionEvent = exceptionEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the exception event profile. */
  def newExceptionEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    exceptionEvent: ExceptionEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    catchLocation: => Option[Location] = Option(exceptionEvent.catchLocation()),
    exception: => ObjectReference = exceptionEvent.exception(),
    exceptionReferenceType: => ReferenceType =exceptionEvent.exception().referenceType(),
    virtualMachine: => VirtualMachine = exceptionEvent.virtualMachine(),
    thread: => ThreadReference = exceptionEvent.thread(),
    threadReferenceType: => ReferenceType = exceptionEvent.thread().referenceType(),
    location: => Location = exceptionEvent.location()
  ): ExceptionEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMethodEntryEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: JDIArgument*
  ): MethodEntryEventInfo = newMethodEntryEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    methodEntryEvent = methodEntryEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the method entry event profile. */
  def newMethodEntryEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    method: => Method = methodEntryEvent.method(),
    virtualMachine: => VirtualMachine = methodEntryEvent.virtualMachine(),
    thread: => ThreadReference = methodEntryEvent.thread(),
    threadReferenceType: => ReferenceType = methodEntryEvent.thread().referenceType(),
    location: => Location = methodEntryEvent.location()
  ): MethodEntryEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMethodExitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: JDIArgument*
  ): MethodExitEventInfo = newMethodExitEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    methodExitEvent = methodExitEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the method exit event profile. */
  def newMethodExitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    method: => Method = methodExitEvent.method(),
    returnValue: => Value = methodExitEvent.returnValue(),
    virtualMachine: => VirtualMachine = methodExitEvent.virtualMachine(),
    thread: => ThreadReference = methodExitEvent.thread(),
    threadReferenceType: => ReferenceType = methodExitEvent.thread().referenceType(),
    location: => Location = methodExitEvent.location()
  ): MethodExitEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultModificationWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: JDIArgument*
  ): ModificationWatchpointEventInfo = newModificationWatchpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    modificationWatchpointEvent = modificationWatchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the modification watchpoint event profile. */
  def newModificationWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: Seq[JDIArgument]
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
  ): ModificationWatchpointEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMonitorContendedEnteredEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: JDIArgument*
  ): MonitorContendedEnteredEventInfo = newMonitorContendedEnteredEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorContendedEnteredEvent = monitorContendedEnteredEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended entered event profile. */
  def newMonitorContendedEnteredEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference = monitorContendedEnteredEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorContendedEnteredEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorContendedEnteredEvent.virtualMachine(),
    thread: => ThreadReference = monitorContendedEnteredEvent.thread(),
    threadReferenceType: => ReferenceType = monitorContendedEnteredEvent.thread().referenceType(),
    location: => Location = monitorContendedEnteredEvent.location()
  ): MonitorContendedEnteredEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMonitorContendedEnterEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: JDIArgument*
  ): MonitorContendedEnterEventInfo = newMonitorContendedEnterEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorContendedEnterEvent = monitorContendedEnterEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended enter event profile. */
  def newMonitorContendedEnterEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference = monitorContendedEnterEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorContendedEnterEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorContendedEnterEvent.virtualMachine(),
    thread: => ThreadReference = monitorContendedEnterEvent.thread(),
    threadReferenceType: => ReferenceType = monitorContendedEnterEvent.thread().referenceType(),
    location: => Location = monitorContendedEnterEvent.location()
  ): MonitorContendedEnterEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMonitorWaitedEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: JDIArgument*
  ): MonitorWaitedEventInfo = newMonitorWaitedEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorWaitedEvent = monitorWaitedEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor waited event profile. */
  def newMonitorWaitedEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference = monitorWaitedEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorWaitedEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorWaitedEvent.virtualMachine(),
    thread: => ThreadReference = monitorWaitedEvent.thread(),
    threadReferenceType: => ReferenceType = monitorWaitedEvent.thread().referenceType(),
    location: => Location = monitorWaitedEvent.location()
  ): MonitorWaitedEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultMonitorWaitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: JDIArgument*
  ): MonitorWaitEventInfo = newMonitorWaitEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorWaitEvent = monitorWaitEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor wait event profile. */
  def newMonitorWaitEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference = monitorWaitEvent.monitor(),
    monitorReferenceType: => ReferenceType = monitorWaitEvent.monitor().referenceType(),
    virtualMachine: => VirtualMachine = monitorWaitEvent.virtualMachine(),
    thread: => ThreadReference = monitorWaitEvent.thread(),
    threadReferenceType: => ReferenceType = monitorWaitEvent.thread().referenceType(),
    location: => Location = monitorWaitEvent.location()
  ): MonitorWaitEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultStepEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: JDIArgument*
  ): StepEventInfo = newStepEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    stepEvent = stepEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the step event profile. */
  def newStepEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = stepEvent.virtualMachine(),
    thread: => ThreadReference = stepEvent.thread(),
    threadReferenceType: => ReferenceType = stepEvent.thread().referenceType(),
    location: => Location = stepEvent.location()
  ): StepEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: JDIArgument*
  ): ThreadDeathEventInfo = newThreadDeathEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadDeathEvent = threadDeathEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the thread death event profile. */
  def newThreadDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = threadDeathEvent.virtualMachine(),
    thread: => ThreadReference = threadDeathEvent.thread(),
    threadReferenceType: => ReferenceType = threadDeathEvent.thread().referenceType()
  ): ThreadDeathEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: JDIArgument*
  ): ThreadStartEventInfo = newThreadStartEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadStartEvent = threadStartEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the thread start event profile. */
  def newThreadStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = threadStartEvent.virtualMachine(),
    thread: => ThreadReference = threadStartEvent.thread(),
    threadReferenceType: => ReferenceType = threadStartEvent.thread().referenceType()
  ): ThreadStartEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: JDIArgument*
  ): VMDeathEventInfo = newVMDeathEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    vmDeathEvent = vmDeathEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the vm death event profile. */
  def newVMDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDeathEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMDisconnectEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: JDIArgument*
  ): VMDisconnectEventInfo = newVMDisconnectEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    vmDisconnectEvent = vmDisconnectEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the vm disconnect event profile. */
  def newVMDisconnectEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDisconnectEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: JDIArgument*
  ): VMStartEventInfo = newVMStartEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    vmStartEvent = vmStartEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the vm start event profile. */
  def newVMStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = vmStartEvent.virtualMachine(),
    thread: => ThreadReference = vmStartEvent.thread(),
    threadReferenceType: => ReferenceType = vmStartEvent.thread().referenceType()
  ): VMStartEventInfo
}
