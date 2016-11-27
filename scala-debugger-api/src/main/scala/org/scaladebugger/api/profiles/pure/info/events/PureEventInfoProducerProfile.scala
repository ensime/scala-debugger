package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface to produce pure event info profile instances.
 *
 * @param infoProducer The parent information producer of this event
 *                     information producer
 */
class PureEventInfoProducerProfile(
  val infoProducer: InfoProducerProfile
) extends EventInfoProducerProfile {
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
  override def toJavaInfo: EventInfoProducerProfile = {
    new PureEventInfoProducerProfile(infoProducer.toJavaInfo)
  }

  override def newEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    event: Event,
    jdiArguments: JDIArgument*
  ): EventInfoProfile = new PureEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    event = event,
    jdiArguments = jdiArguments
  )

  override def newLocatableEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    locatableEvent: LocatableEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): LocatableEventInfoProfile = new PureLocatableEventInfoProfile(
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

  override def newWatchpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    watchpointEvent: WatchpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): WatchpointEventInfoProfile = new PureWatchpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    watchpointEvent = watchpointEvent,
    jdiArguments = jdiArguments
  )(
    _virtualMachine = virtualMachine,
    _thread = thread,
    _threadReferenceType = threadReferenceType,
    _location = location
  )

  override def newAccessWatchpointEventInfoProfile(
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
  ): AccessWatchpointEventInfoProfile = new PureAccessWatchpointEventInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    infoProducer = infoProducer,
    accessWatchpointEvent = accessWatchpointEvent,
    jdiArguments = jdiArguments
  )

  override def newBreakpointEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    breakpointEvent: BreakpointEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): BreakpointEventInfoProfile = ???

  override def newClassPrepareEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classPrepareEvent: ClassPrepareEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    referenceType: => ReferenceType
  ): ClassPrepareEventInfoProfile = ???

  override def newClassUnloadEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    classUnloadEvent: ClassUnloadEvent,
    jdiArguments: JDIArgument*
  ): ClassUnloadEventInfoProfile = ???

  override def newExceptionEventInfoProfile(
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
  ): ExceptionEventInfoProfile = ???

  override def newMethodEntryEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    methodEntryEvent: MethodEntryEvent,
    jdiArguments: JDIArgument*
  )(
    method: => Method,
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): MethodEntryEventInfoProfile = ???

  override def newMethodExitEventInfoProfile(
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
  ): MethodExitEventInfoProfile = ???

  override def newModificationWatchpointEventInfoProfile(
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
  ): ModificationWatchpointEventInfoProfile = ???

  override def newMonitorContendedEnteredEventInfoProfile(
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
  ): MonitorContendedEnteredEventInfoProfile = ???

  override def newMonitorContendedEnterEventInfoProfile(
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
  ): MonitorContendedEnterEventInfoProfile = ???

  override def newMonitorWaitedEventInfoProfile(
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
  ): MonitorWaitedEventInfoProfile = ???

  override def newMonitorWaitEventInfoProfile(
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
  ): MonitorWaitEventInfoProfile = ???

  override def newStepEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    stepEvent: StepEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType,
    location: => Location
  ): StepEventInfoProfile = ???

  override def newThreadDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadDeathEvent: ThreadDeathEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadDeathEventInfoProfile = ???

  override def newThreadStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    threadStartEvent: ThreadStartEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): ThreadStartEventInfoProfile = ???

  override def newVMDeathEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDeathEvent: VMDeathEvent,
    jdiArguments: JDIArgument*
  ): VMDeathEventInfoProfile = ???

  override def newVMDisconnectEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmDisconnectEvent: VMDisconnectEvent,
    jdiArguments: JDIArgument*
  ): VMDisconnectEventInfoProfile = ???

  override def newVMStartEventInfoProfile(
    scalaVirtualMachine: ScalaVirtualMachine,
    vmStartEvent: VMStartEvent,
    jdiArguments: JDIArgument*
  )(
    virtualMachine: => VirtualMachine,
    thread: => ThreadReference,
    threadReferenceType: => ReferenceType
  ): VMStartEventInfoProfile = ???
}
