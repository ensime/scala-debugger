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
  def newDefaultEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: JDIArgument*
  ): EventInfo = newEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    event = event,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the event profile. */
  def newEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: Seq[JDIArgument]
  ): EventInfo

  /** Fills in additional properties with default values. */
  def newDefaultLocatableEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: JDIArgument*
  ): LocatableEventInfo = newLocatableEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    locatableEvent = locatableEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the locatable event profile. */
  def newLocatableEventInfo(
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
  def newDefaultMonitorEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorEvent: MonitorEvent,
    jdiArguments: JDIArgument*
  ): MonitorEventInfo = newMonitorEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorEvent = monitorEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended entered event profile. */
  def newMonitorEventInfo(
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
  def newDefaultWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: JDIArgument*
  ): WatchpointEventInfo = newWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    watchpointEvent = watchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the watchpoint event profile. */
  def newWatchpointEventInfo(
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
  def newDefaultAccessWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: JDIArgument*
  ): AccessWatchpointEventInfo = newAccessWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    accessWatchpointEvent = accessWatchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the access watchpoint event profile. */
  def newAccessWatchpointEventInfo(
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
  def newDefaultBreakpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: JDIArgument*
  ): BreakpointEventInfo = newBreakpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    breakpointEvent = breakpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the breakpoint event profile. */
  def newBreakpointEventInfo(
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
  def newDefaultClassPrepareEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: JDIArgument*
  ): ClassPrepareEventInfo = newClassPrepareEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    classPrepareEvent = classPrepareEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the class prepare event profile. */
  def newClassPrepareEventInfo(
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
  def newDefaultClassUnloadEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: JDIArgument*
  ): ClassUnloadEventInfo = newClassUnloadEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    classUnloadEvent = classUnloadEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the class unload event profile. */
  def newClassUnloadEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: Seq[JDIArgument]
  ): ClassUnloadEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultExceptionEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    exceptionEvent: ExceptionEvent,
    jdiArguments: JDIArgument*
  ): ExceptionEventInfo = newExceptionEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    exceptionEvent = exceptionEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the exception event profile. */
  def newExceptionEventInfo(
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
  def newDefaultMethodEntryEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: JDIArgument*
  ): MethodEntryEventInfo = newMethodEntryEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    methodEntryEvent = methodEntryEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the method entry event profile. */
  def newMethodEntryEventInfo(
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
  def newDefaultMethodExitEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: JDIArgument*
  ): MethodExitEventInfo = newMethodExitEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    methodExitEvent = methodExitEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the method exit event profile. */
  def newMethodExitEventInfo(
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
  def newDefaultModificationWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: JDIArgument*
  ): ModificationWatchpointEventInfo = newModificationWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    modificationWatchpointEvent = modificationWatchpointEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the modification watchpoint event profile. */
  def newModificationWatchpointEventInfo(
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
  def newDefaultMonitorContendedEnteredEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: JDIArgument*
  ): MonitorContendedEnteredEventInfo = newMonitorContendedEnteredEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorContendedEnteredEvent = monitorContendedEnteredEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended entered event profile. */
  def newMonitorContendedEnteredEventInfo(
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
  def newDefaultMonitorContendedEnterEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: JDIArgument*
  ): MonitorContendedEnterEventInfo = newMonitorContendedEnterEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorContendedEnterEvent = monitorContendedEnterEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor contended enter event profile. */
  def newMonitorContendedEnterEventInfo(
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
  def newDefaultMonitorWaitedEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: JDIArgument*
  ): MonitorWaitedEventInfo = newMonitorWaitedEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorWaitedEvent = monitorWaitedEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor waited event profile. */
  def newMonitorWaitedEventInfo(
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
  def newDefaultMonitorWaitEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: JDIArgument*
  ): MonitorWaitEventInfo = newMonitorWaitEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    monitorWaitEvent = monitorWaitEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the monitor wait event profile. */
  def newMonitorWaitEventInfo(
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
  def newDefaultStepEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: JDIArgument*
  ): StepEventInfo = newStepEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    stepEvent = stepEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the step event profile. */
  def newStepEventInfo(
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
  def newDefaultThreadDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: JDIArgument*
  ): ThreadDeathEventInfo = newThreadDeathEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    threadDeathEvent = threadDeathEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the thread death event profile. */
  def newThreadDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = threadDeathEvent.virtualMachine(),
    thread: => ThreadReference = threadDeathEvent.thread(),
    threadReferenceType: => ReferenceType = threadDeathEvent.thread().referenceType()
  ): ThreadDeathEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultThreadStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: JDIArgument*
  ): ThreadStartEventInfo = newThreadStartEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    threadStartEvent = threadStartEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the thread start event profile. */
  def newThreadStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = threadStartEvent.virtualMachine(),
    thread: => ThreadReference = threadStartEvent.thread(),
    threadReferenceType: => ReferenceType = threadStartEvent.thread().referenceType()
  ): ThreadStartEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: JDIArgument*
  ): VMDeathEventInfo = newVMDeathEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    vmDeathEvent = vmDeathEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the vm death event profile. */
  def newVMDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDeathEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMDisconnectEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: JDIArgument*
  ): VMDisconnectEventInfo = newVMDisconnectEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    vmDisconnectEvent = vmDisconnectEvent,
    jdiArguments = jdiArguments
  )

  /** Creates a new instance of the vm disconnect event profile. */
  def newVMDisconnectEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDisconnectEventInfo

  /** Fills in additional properties with default values. */
  def newDefaultVMStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: JDIArgument*
  ): VMStartEventInfo = newVMStartEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    vmStartEvent = vmStartEvent,
    jdiArguments = jdiArguments
  )()

  /** Creates a new instance of the vm start event profile. */
  def newVMStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine = vmStartEvent.virtualMachine(),
    thread: => ThreadReference = vmStartEvent.thread(),
    threadReferenceType: => ReferenceType = vmStartEvent.thread().referenceType()
  ): VMStartEventInfo
}
