package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a java implementation of an event info profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param event The event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 */
class JavaEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducer,
  private val event: Event,
  private val jdiArguments: Seq[JDIArgument] = Nil
) extends EventInfo {
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
  override def toJavaInfo: EventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      event = event,
      jdiArguments = jdiArguments
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Event = event

  /**
   * Returns the request arguments provided to this event's request.
   *
   * @return The collection of request arguments
   */
  override lazy val requestArguments: Seq[JDIRequestArgument] = {
    jdiArguments.collect { case r: JDIRequestArgument => r }
  }

  /**
   * Returns the event arguments provided to this event.
   *
   * @return The collection of event arguments
   */
  override lazy val eventArguments: Seq[JDIEventArgument] = {
    jdiArguments.collect { case e: JDIEventArgument => e }
  }

  /**
   * Returns the string representation of the underlying event.
   *
   * @return The event string representation
   */
  override def toString: String = event.toString

  /**
   * Returns whether or not this event is a plain event. By plain, this means
   * that the underlying event does not extend any more specific interface than
   * the low-level event interface.
   *
   * For example, a breakpoint event would return false. Likewise, a locatable
   * event would return false. A raw event would return true.
   *
   * @return True if plain, otherwise false
   */
  override def isPlainEvent: Boolean = eventClass.equals(classOf[Event])

  /**
   * Returns the class of the underlying event.
   *
   * @return The class of the event
   */
  protected def eventClass: Class[_] = event.getClass

  /**
   * Returns whether or not this event represents an access watchpoint event.
   *
   * @return True if an access watchpoint event, otherwise false
   */
  override def isAccessWatchpointEvent: Boolean = {
    event.isInstanceOf[AccessWatchpointEvent]
  }

  /**
   * Returns whether or not this event represents a breakpoint event.
   *
   * @return True if a breakpoint event, otherwise false
   */
  override def isBreakpointEvent: Boolean = {
    event.isInstanceOf[BreakpointEvent]
  }

  /**
   * Returns whether or not this event represents a class prepare event.
   *
   * @return True if a class prepare event, otherwise false
   */
  override def isClassPrepareEvent: Boolean = {
    event.isInstanceOf[ClassPrepareEvent]
  }

  /**
   * Returns whether or not this event represents a class unload event.
   *
   * @return True if a class unload event, otherwise false
   */
  override def isClassUnloadEvent: Boolean = {
    event.isInstanceOf[ClassUnloadEvent]
  }

  /**
   * Returns whether or not this event represents an exception event.
   *
   * @return True if an exception event, otherwise false
   */
  override def isExceptionEvent: Boolean = {
    event.isInstanceOf[ExceptionEvent]
  }

  /**
   * Returns whether or not this event represents a locatable event.
   *
   * @return True if a locatable event, otherwise false
   */
  override def isLocatableEvent: Boolean = {
    event.isInstanceOf[LocatableEvent]
  }

  /**
   * Returns whether or not this event represents a method entry event.
   *
   * @return True if a method entry event, otherwise false
   */
  override def isMethodEntryEvent: Boolean = {
    event.isInstanceOf[MethodEntryEvent]
  }

  /**
   * Returns whether or not this event represents a method exit event.
   *
   * @return True if a method exit event, otherwise false
   */
  override def isMethodExitEvent: Boolean = {
    event.isInstanceOf[MethodExitEvent]
  }

  /**
   * Returns whether or not this event represents a
   * modification watchpoint event.
   *
   * @return True if a modification watchpoint event, otherwise false
   */
  override def isModificationWatchpointEvent: Boolean = {
    event.isInstanceOf[ModificationWatchpointEvent]
  }

  /**
   * Returns whether or not this event represents a
   * monitor contended entered event.
   *
   * @return True if a monitor contended entered event, otherwise false
   */
  override def isMonitorContendedEnteredEvent: Boolean = {
    event.isInstanceOf[MonitorContendedEnteredEvent]
  }

  /**
   * Returns whether or not this event represents a
   * monitor contended enter event.
   *
   * @return True if a monitor contended enter event, otherwise false
   */
  override def isMonitorContendedEnterEvent: Boolean = {
    event.isInstanceOf[MonitorContendedEnterEvent]
  }

  /**
   * Returns whether or not this event represents a
   * monitor waited event.
   *
   * @return True if a monitor waited event, otherwise false
   */
  override def isMonitorWaitedEvent: Boolean = {
    event.isInstanceOf[MonitorWaitedEvent]
  }

  /**
   * Returns whether or not this event represents a
   * monitor wait event.
   *
   * @return True if a monitor wait event, otherwise false
   */
  override def isMonitorWaitEvent: Boolean = {
    event.isInstanceOf[MonitorWaitEvent]
  }

  /**
   * Returns whether or not this event represents a step event.
   *
   * @return True if a step event, otherwise false
   */
  override def isStepEvent: Boolean = {
    event.isInstanceOf[StepEvent]
  }

  /**
   * Returns whether or not this event represents a thread death event.
   *
   * @return True if a thread death event, otherwise false
   */
  override def isThreadDeathEvent: Boolean = {
    event.isInstanceOf[ThreadDeathEvent]
  }

  /**
   * Returns whether or not this event represents a thread start event.
   *
   * @return True if a thread start event, otherwise false
   */
  override def isThreadStartEvent: Boolean = {
    event.isInstanceOf[ThreadStartEvent]
  }

  /**
   * Returns whether or not this event represents a vm death event.
   *
   * @return True if a vm death event, otherwise false
   */
  override def isVMDeathEvent: Boolean = {
    event.isInstanceOf[VMDeathEvent]
  }

  /**
   * Returns whether or not this event represents a vm disconnect event.
   *
   * @return True if a vm disconnect event, otherwise false
   */
  override def isVMDisconnectEvent: Boolean = {
    event.isInstanceOf[VMDisconnectEvent]
  }

  /**
   * Returns whether or not this event represents a vm start event.
   *
   * @return True if a vm start event, otherwise false
   */
  override def isVMStartEvent: Boolean = {
    event.isInstanceOf[VMStartEvent]
  }

  /**
   * Returns whether or not this event represents a watchpoint event.
   *
   * @return True if a watchpoint event, otherwise false
   */
  override def isWatchpointEvent: Boolean = {
    event.isInstanceOf[WatchpointEvent]
  }

  /**
   * Returns the event as an access watchpoint event.
   *
   * @return The access watchpoint event profile wrapping the event
   * @throws AssertionError If not an access watchpoint event
   */
  override def toAccessWatchpointEvent: AccessWatchpointEventInfo = {
    assert(isAccessWatchpointEvent, "Event must be an access watchpoint event!")
    infoProducer.eventProducer.newDefaultAccessWatchpointEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      accessWatchpointEvent = event.asInstanceOf[AccessWatchpointEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a breakpoint event.
   *
   * @return The breakpoint event profile wrapping the event
   * @throws AssertionError If not a breakpoint event
   */
  override def toBreakpointEvent: BreakpointEventInfo = {
    assert(isBreakpointEvent, "Event must be a breakpoint event!")
    infoProducer.eventProducer.newDefaultBreakpointEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      breakpointEvent = event.asInstanceOf[BreakpointEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a class prepare event.
   *
   * @return The class prepare event profile wrapping the event
   * @throws AssertionError If not a class prepare event
   */
  override def toClassPrepareEvent: ClassPrepareEventInfo = {
    assert(isClassPrepareEvent, "Event must be a class prepare event!")
    infoProducer.eventProducer.newDefaultClassPrepareEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      classPrepareEvent = event.asInstanceOf[ClassPrepareEvent],
      jdiArguments: _*
    )
  }

  /** :w
   * Returns the event as a class unload event.
   *
   * @return The class unload event profile wrapping the event
   * @throws AssertionError If not a class unload event
   */
  override def toClassUnloadEvent: ClassUnloadEventInfo = {
    assert(isClassUnloadEvent, "Event must be a class unload event!")
    infoProducer.eventProducer.newDefaultClassUnloadEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      classUnloadEvent = event.asInstanceOf[ClassUnloadEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as an exception event.
   *
   * @return The exception event profile wrapping the event
   * @throws AssertionError If not an exception event
   */
  override def toExceptionEvent: ExceptionEventInfo = {
    assert(isExceptionEvent, "Event must be an exception event!")
    infoProducer.eventProducer.newDefaultExceptionEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      exceptionEvent = event.asInstanceOf[ExceptionEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a locatable event.
   *
   * @return The locatable event profile wrapping the event
   * @throws AssertionError If not a locatable event
   */
  override def toLocatableEvent: LocatableEventInfo = {
    assert(isLocatableEvent, "Event must be a locatable event!")
    infoProducer.eventProducer.newDefaultLocatableEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      locatableEvent = event.asInstanceOf[LocatableEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a method entry event.
   *
   * @return The method entry event profile wrapping the event
   * @throws AssertionError If not a method entry event
   */
  override def toMethodEntryEvent: MethodEntryEventInfo = {
    assert(isMethodEntryEvent, "Event must be a method entry event!")
    infoProducer.eventProducer.newDefaultMethodEntryEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      methodEntryEvent = event.asInstanceOf[MethodEntryEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a method exit event.
   *
   * @return The method exit event profile wrapping the event
   * @throws AssertionError If not a method exit event
   */
  override def toMethodExitEvent: MethodExitEventInfo = {
    assert(isMethodExitEvent, "Event must be a method exit event!")
    infoProducer.eventProducer.newDefaultMethodExitEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      methodExitEvent = event.asInstanceOf[MethodExitEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as an modification watchpoint event.
   *
   * @return The modification watchpoint event profile wrapping the event
   * @throws AssertionError If not an modification watchpoint event
   */
  override def toModificationWatchpointEvent: ModificationWatchpointEventInfo = {
    assert(isModificationWatchpointEvent, "Event must be a modification watchpoint event!")
    infoProducer.eventProducer.newDefaultModificationWatchpointEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      modificationWatchpointEvent = event.asInstanceOf[ModificationWatchpointEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a monitor event.
   *
   * @return The monitor event profile wrapping the event
   * @throws AssertionError If not a monitor event
   */
  override def toMonitorEvent: MonitorEventInfo = {
    assert(isMonitorEvent, "Event must be a monitor event!")
    infoProducer.eventProducer.newDefaultMonitorEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorEvent = newMonitorEvent(event.asInstanceOf[LocatableEvent]),
      jdiArguments: _*
    )
  }

  /**
   * Creates a new instance of a monitor event from a locatable event.
   *
   * @param locatableEvent The locatable event to wrap in a monitor event
   * @return The new monitor event instance
   */
  protected def newMonitorEvent(locatableEvent: LocatableEvent): MonitorEvent =
    new MonitorEvent(locatableEvent)

  /**
   * Returns the event as a monitor contended entered event.
   *
   * @return The monitor contended entered event profile wrapping the event
   * @throws AssertionError If not a monitor contended entered event
   */
  override def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfo = {
    assert(isMonitorContendedEnteredEvent, "Event must be a monitor contended entered event!")
    infoProducer.eventProducer.newDefaultMonitorContendedEnteredEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorContendedEnteredEvent = event.asInstanceOf[MonitorContendedEnteredEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a monitor contended enter event.
   *
   * @return The monitor contended enter event profile wrapping the event
   * @throws AssertionError If not a monitor contended enter event
   */
  override def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfo = {
    assert(isMonitorContendedEnterEvent, "Event must be a monitor contended enter event!")
    infoProducer.eventProducer.newDefaultMonitorContendedEnterEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorContendedEnterEvent = event.asInstanceOf[MonitorContendedEnterEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a monitor waited event.
   *
   * @return The monitor waited event profile wrapping the event
   * @throws AssertionError If not a monitor waited event
   */
  override def toMonitorWaitedEvent: MonitorWaitedEventInfo = {
    assert(isMonitorWaitedEvent, "Event must be a monitor waited event!")
    infoProducer.eventProducer.newDefaultMonitorWaitedEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorWaitedEvent = event.asInstanceOf[MonitorWaitedEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a monitor wait event.
   *
   * @return The monitor wait event profile wrapping the event
   * @throws AssertionError If not a monitor wait event
   */
  override def toMonitorWaitEvent: MonitorWaitEventInfo = {
    assert(isMonitorWaitEvent, "Event must be a monitor wait event!")
    infoProducer.eventProducer.newDefaultMonitorWaitEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorWaitEvent = event.asInstanceOf[MonitorWaitEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a step event.
   *
   * @return The step event profile wrapping the event
   * @throws AssertionError If not a step event
   */
  override def toStepEvent: StepEventInfo = {
    assert(isStepEvent, "Event must be a step event!")
    infoProducer.eventProducer.newDefaultStepEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      stepEvent = event.asInstanceOf[StepEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a thread death event.
   *
   * @return The thread death event profile wrapping the event
   * @throws AssertionError If not a thread death event
   */
  override def toThreadDeathEvent: ThreadDeathEventInfo = {
    assert(isThreadDeathEvent, "Event must be a thread death event!")
    infoProducer.eventProducer.newDefaultThreadDeathEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      threadDeathEvent = event.asInstanceOf[ThreadDeathEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a thread start event.
   *
   * @return The thread start event profile wrapping the event
   * @throws AssertionError If not a thread start event
   */
  override def toThreadStartEvent: ThreadStartEventInfo = {
    assert(isThreadStartEvent, "Event must be a thread start event!")
    infoProducer.eventProducer.newDefaultThreadStartEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      threadStartEvent = event.asInstanceOf[ThreadStartEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a vm death event.
   *
   * @return The vm death event profile wrapping the event
   * @throws AssertionError If not a vm death event
   */
  override def toVMDeathEvent: VMDeathEventInfo = {
    assert(isVMDeathEvent, "Event must be a vm death event!")
    infoProducer.eventProducer.newDefaultVMDeathEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      vmDeathEvent = event.asInstanceOf[VMDeathEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a vm disconnect event.
   *
   * @return The vm disconnect event profile wrapping the event
   * @throws AssertionError If not a vm disconnect event
   */
  override def toVMDisconnectEvent: VMDisconnectEventInfo = {
    assert(isVMDisconnectEvent, "Event must be a vm disconnect event!")
    infoProducer.eventProducer.newDefaultVMDisconnectEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      vmDisconnectEvent = event.asInstanceOf[VMDisconnectEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a vm start event.
   *
   * @return The vm start event profile wrapping the event
   * @throws AssertionError If not a vm start event
   */
  override def toVMStartEvent: VMStartEventInfo = {
    assert(isVMStartEvent, "Event must be a vm start event!")
    infoProducer.eventProducer.newDefaultVMStartEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      vmStartEvent = event.asInstanceOf[VMStartEvent],
      jdiArguments: _*
    )
  }

  /**
   * Returns the event as a watchpoint event.
   *
   * @return The watchpoint event profile wrapping the event
   * @throws AssertionError If not a watchpoint event
   */
  override def toWatchpointEvent: WatchpointEventInfo = {
    assert(isWatchpointEvent, "Event must be a watchpoint event!")
    infoProducer.eventProducer.newDefaultWatchpointEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      watchpointEvent = event.asInstanceOf[WatchpointEvent],
      jdiArguments: _*
    )
  }
}
