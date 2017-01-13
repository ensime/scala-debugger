package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce pure event info profile instances.
 *
 * @param infoProducer The parent information producer of this event
 *                     information producer
 */
class PureEventInfoProducer(
  val infoProducer: InfoProducer
) extends EventInfoProducer {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: EventInfoProducer = {
    new PureEventInfoProducer(infoProducer.toJavaInfo)
  }

  override def newEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: Seq[JDIArgument]
  ): EventInfo = new PureEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    event = event,
    jdiArguments = jdiArguments
  )

  override def newLocatableEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): LocatableEventInfo = new PureLocatableEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    locatableEvent = locatableEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    container: => Either[ObjectReference, ReferenceType],
    field: => Field,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): WatchpointEventInfo = new PureWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    watchpointEvent = watchpointEvent,
    jdiArguments = jdiArguments
  )(
    _container = container,
    _field = field,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newAccessWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    accessWatchpointEvent: AccessWatchpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    container: => Either[ObjectReference, ReferenceType],
    field: => Field,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): AccessWatchpointEventInfo = new PureAccessWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    accessWatchpointEvent = accessWatchpointEvent,
    jdiArguments = jdiArguments
  )(
    _container = container,
    _field = field,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newBreakpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): BreakpointEventInfo = new PureBreakpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    breakpointEvent = breakpointEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newClassPrepareEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    referenceType: => ReferenceType
  ): ClassPrepareEventInfo = new PureClassPrepareEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    classPrepareEvent = classPrepareEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _referenceType = referenceType
  )

  override def newClassUnloadEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: Seq[JDIArgument]
  ): ClassUnloadEventInfo = new PureClassUnloadEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    classUnloadEvent = classUnloadEvent,
    jdiArguments = jdiArguments
  )

  override def newExceptionEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    exceptionEvent: ExceptionEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    catchLocation: => Option[Location],
    exception: => ObjectReference,
    exceptionReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): ExceptionEventInfo = new PureExceptionEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    exceptionEvent = exceptionEvent,
    jdiArguments = jdiArguments
  )(
    _catchLocation = catchLocation,
    _exception = exception,
    _exceptionReferenceType = exceptionReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMethodEntryEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    method: => Method,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MethodEntryEventInfo = new PureMethodEntryEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    methodEntryEvent = methodEntryEvent,
    jdiArguments = jdiArguments
  )(
    _method = method,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMethodExitEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodExitEvent: MethodExitEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    method: => Method,
    returnValue: => Value,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MethodExitEventInfo = new PureMethodExitEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    methodExitEvent = methodExitEvent,
    jdiArguments = jdiArguments
  )(
    _method = method,
    _returnValue = returnValue,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newModificationWatchpointEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    modificationWatchpointEvent: ModificationWatchpointEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    container: => Either[ObjectReference, ReferenceType],
    field: => Field,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): ModificationWatchpointEventInfo = new PureModificationWatchpointEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    modificationWatchpointEvent = modificationWatchpointEvent,
    jdiArguments = jdiArguments
  )(
    _container = container,
    _field = field,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMonitorEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorEvent: MonitorEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorEventInfo = new PureMonitorEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    monitorEvent = monitorEvent,
    jdiArguments = jdiArguments
  )(
    _monitor = monitor,
    _monitorReferenceType = monitorReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMonitorContendedEnteredEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnteredEvent: MonitorContendedEnteredEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorContendedEnteredEventInfo = new PureMonitorContendedEnteredEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    monitorContendedEnteredEvent = monitorContendedEnteredEvent,
    jdiArguments = jdiArguments
  )(
    _monitor = monitor,
    _monitorReferenceType = monitorReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMonitorContendedEnterEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorContendedEnterEvent: MonitorContendedEnterEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorContendedEnterEventInfo = new PureMonitorContendedEnterEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    monitorContendedEnterEvent = monitorContendedEnterEvent,
    jdiArguments = jdiArguments
  )(
    _monitor = monitor,
    _monitorReferenceType = monitorReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMonitorWaitedEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitedEvent: MonitorWaitedEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorWaitedEventInfo = new PureMonitorWaitedEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    monitorWaitedEvent = monitorWaitedEvent,
    jdiArguments = jdiArguments
  )(
    _monitor = monitor,
    _monitorReferenceType = monitorReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newMonitorWaitEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    monitorWaitEvent: MonitorWaitEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    monitor: => ObjectReference,
    monitorReferenceType: => ReferenceType,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MonitorWaitEventInfo = new PureMonitorWaitEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    monitorWaitEvent = monitorWaitEvent,
    jdiArguments = jdiArguments
  )(
    _monitor = monitor,
    _monitorReferenceType = monitorReferenceType,
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newStepEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): StepEventInfo = new PureStepEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    stepEvent = stepEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newThreadDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadDeathEventInfo = new PureThreadDeathEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    threadDeathEvent = threadDeathEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType
  )

  override def newThreadStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadStartEventInfo = new PureThreadStartEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    threadStartEvent = threadStartEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType
  )

  override def newVMDeathEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDeathEventInfo = new PureVMDeathEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    vmDeathEvent = vmDeathEvent,
    jdiArguments = jdiArguments
  )

  override def newVMDisconnectEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: Seq[JDIArgument]
  ): VMDisconnectEventInfo = new PureVMDisconnectEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    vmDisconnectEvent = vmDisconnectEvent,
    jdiArguments = jdiArguments
  )

  override def newVMStartEventInfo(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: Seq[JDIArgument]
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): VMStartEventInfo = new PureVMStartEventInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    vmStartEvent = vmStartEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType
  )
}
