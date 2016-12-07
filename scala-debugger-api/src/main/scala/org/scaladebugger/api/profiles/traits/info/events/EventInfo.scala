package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.CommonInfo

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * an abstraction over the JDI event interface.
 */
trait EventInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: EventInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Event

  /**
   * Returns all arguments provided to this event.
   *
   * @return The collection of request and event arguments associated with
   *         this event and its request
   */
  def allArguments: Seq[JDIArgument] = requestArguments ++ eventArguments

  /**
   * Returns the request arguments provided to this event's request.
   *
   * @return The collection of request arguments
   */
  def requestArguments: Seq[JDIRequestArgument]

  /**
   * Returns the event arguments provided to this event.
   *
   * @return The collection of event arguments
   */
  def eventArguments: Seq[JDIEventArgument]

  /**
   * Returns whether or not this event represents an access watchpoint event.
   *
   * @return True if an access watchpoint event, otherwise false
   */
  def isAccessWatchpointEvent: Boolean

  /**
   * Returns whether or not this event represents a breakpoint event.
   *
   * @return True if a breakpoint event, otherwise false
   */
  def isBreakpointEvent: Boolean

  /**
   * Returns whether or not this event represents a class prepare event.
   *
   * @return True if a class prepare event, otherwise false
   */
  def isClassPrepareEvent: Boolean

  /**
   * Returns whether or not this event represents a class unload event.
   *
   * @return True if a class unload event, otherwise false
   */
  def isClassUnloadEvent: Boolean

  /**
   * Returns whether or not this event represents an exception event.
   *
   * @return True if an exception event, otherwise false
   */
  def isExceptionEvent: Boolean

  /**
   * Returns whether or not this event represents a locatable event.
   *
   * @return True if a locatable event, otherwise false
   */
  def isLocatableEvent: Boolean

  /**
   * Returns whether or not this event represents a method entry event.
   *
   * @return True if a method entry event, otherwise false
   */
  def isMethodEntryEvent: Boolean

  /**
   * Returns whether or not this event represents a method exit event.
   *
   * @return True if a method exit event, otherwise false
   */
  def isMethodExitEvent: Boolean

  /**
   * Returns whether or not this event represents a
   * modification watchpoint event.
   *
   * @return True if a modification watchpoint event, otherwise false
   */
  def isModificationWatchpointEvent: Boolean

  /**
   * Returns whether or not this event represents any
   * monitor event.
   *
   * @return True if any monitor event, otherwise false
   */
  def isMonitorEvent: Boolean = {
    isMonitorContendedEnteredEvent || isMonitorContendedEnterEvent ||
    isMonitorWaitedEvent || isMonitorWaitEvent
  }

  /**
   * Returns whether or not this event represents a
   * monitor contended entered event.
   *
   * @return True if a monitor contended entered event, otherwise false
   */
  def isMonitorContendedEnteredEvent: Boolean

  /**
   * Returns whether or not this event represents a
   * monitor contended enter event.
   *
   * @return True if a monitor contended enter event, otherwise false
   */
  def isMonitorContendedEnterEvent: Boolean

  /**
   * Returns whether or not this event represents a
   * monitor waited event.
   *
   * @return True if a monitor waited event, otherwise false
   */
  def isMonitorWaitedEvent: Boolean

  /**
   * Returns whether or not this event represents a
   * monitor wait event.
   *
   * @return True if a monitor wait event, otherwise false
   */
  def isMonitorWaitEvent: Boolean

  /**
   * Returns whether or not this event represents a step event.
   *
   * @return True if a step event, otherwise false
   */
  def isStepEvent: Boolean

  /**
   * Returns whether or not this event represents a thread death event.
   *
   * @return True if a thread death event, otherwise false
   */
  def isThreadDeathEvent: Boolean

  /**
   * Returns whether or not this event represents a thread start event.
   *
   * @return True if a thread start event, otherwise false
   */
  def isThreadStartEvent: Boolean

  /**
   * Returns whether or not this event represents a vm death event.
   *
   * @return True if a vm death event, otherwise false
   */
  def isVMDeathEvent: Boolean

  /**
   * Returns whether or not this event represents a vm disconnect event.
   *
   * @return True if a vm disconnect event, otherwise false
   */
  def isVMDisconnectEvent: Boolean

  /**
   * Returns whether or not this event represents a vm start event.
   *
   * @return True if a vm start event, otherwise false
   */
  def isVMStartEvent: Boolean

  /**
   * Returns whether or not this event represents a watchpoint event.
   *
   * @return True if a watchpoint event, otherwise false
   */
  def isWatchpointEvent: Boolean

  /**
   * Returns the event as an access watchpoint event.
   *
   * @return Success containing the access watchpoint event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToAccessWatchpointEvent: Try[AccessWatchpointEventInfo] = {
    Try(toAccessWatchpointEvent)
  }

  /**
   * Returns the event as an access watchpoint event.
   *
   * @return The access watchpoint event profile wrapping the event
   * @throws AssertionError If not an access watchpoint event
   */
  @throws[AssertionError]
  def toAccessWatchpointEvent: AccessWatchpointEventInfo

  /**
   * Returns the event as a breakpoint event.
   *
   * @return Success containing the breakpoint event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToBreakpointEvent: Try[BreakpointEventInfo] = {
    Try(toBreakpointEvent)
  }

  /**
   * Returns the event as a breakpoint event.
   *
   * @return The breakpoint event profile wrapping the event
   * @throws AssertionError If not a breakpoint event
   */
  @throws[AssertionError]
  def toBreakpointEvent: BreakpointEventInfo

  /**
   * Returns the event as a class prepare event.
   *
   * @return Success containing the class prepare event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToClassPrepareEvent: Try[ClassPrepareEventInfo] = {
    Try(toClassPrepareEvent)
  }

  /**
   * Returns the event as a class prepare event.
   *
   * @return The class prepare event profile wrapping the event
   * @throws AssertionError If not a class prepare event
   */
  @throws[AssertionError]
  def toClassPrepareEvent: ClassPrepareEventInfo

  /**
   * Returns the event as a class unload event.
   *
   * @return Success containing the class unload event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToClassUnloadEvent: Try[ClassUnloadEventInfo] = {
    Try(toClassUnloadEvent)
  }

  /**:w
   * Returns the event as a class unload event.
   *
   * @return The class unload event profile wrapping the event
   * @throws AssertionError If not a class unload event
   */
  @throws[AssertionError]
  def toClassUnloadEvent: ClassUnloadEventInfo

  /**
   * Returns the event as an exception event.
   *
   * @return Success containing the exception event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToExceptionEvent: Try[ExceptionEventInfo] = {
    Try(toExceptionEvent)
  }

  /**
   * Returns the event as an exception event.
   *
   * @return The exception event profile wrapping the event
   * @throws AssertionError If not an exception event
   */
  @throws[AssertionError]
  def toExceptionEvent: ExceptionEventInfo

  /**
   * Returns the event as a locatable event.
   *
   * @return Success containing the locatable event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToLocatableEvent: Try[LocatableEventInfo] = {
    Try(toLocatableEvent)
  }

  /**
   * Returns the event as a locatable event.
   *
   * @return The locatable event profile wrapping the event
   * @throws AssertionError If not a locatable event
   */
  @throws[AssertionError]
  def toLocatableEvent: LocatableEventInfo

  /**
   * Returns the event as a method entry event.
   *
   * @return Success containing the method entry event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMethodEntryEvent: Try[MethodEntryEventInfo] = {
    Try(toMethodEntryEvent)
  }

  /**
   * Returns the event as a method entry event.
   *
   * @return The method entry event profile wrapping the event
   * @throws AssertionError If not a method entry event
   */
  @throws[AssertionError]
  def toMethodEntryEvent: MethodEntryEventInfo

  /**
   * Returns the event as a method exit event.
   *
   * @return Success containing the method exit event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMethodExitEvent: Try[MethodExitEventInfo] = {
    Try(toMethodExitEvent)
  }

  /**
   * Returns the event as a method exit event.
   *
   * @return The method exit event profile wrapping the event
   * @throws AssertionError If not a method exit event
   */
  @throws[AssertionError]
  def toMethodExitEvent: MethodExitEventInfo

  /**
   * Returns the event as an modification watchpoint event.
   *
   * @return Success containing the modification watchpoint event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToModificationWatchpointEvent: Try[ModificationWatchpointEventInfo] = {
    Try(toModificationWatchpointEvent)
  }

  /**
   * Returns the event as an modification watchpoint event.
   *
   * @return The modification watchpoint event profile wrapping the event
   * @throws AssertionError If not an modification watchpoint event
   */
  @throws[AssertionError]
  def toModificationWatchpointEvent: ModificationWatchpointEventInfo

  /**
   * Returns the event as a monitor event.
   *
   * @return Success containing the monitor event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMonitorEvent: Try[MonitorEventInfo] = {
    Try(toMonitorEvent)
  }

  /**
   * Returns the event as a monitor event.
   *
   * @return The monitor event profile wrapping the event
   * @throws AssertionError If not a monitor event
   */
  @throws[AssertionError]
  def toMonitorEvent: MonitorEventInfo

  /**
   * Returns the event as a monitor contended entered event.
   *
   * @return Success containing the monitor contended entered event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMonitorContendedEnteredEvent: Try[MonitorContendedEnteredEventInfo] = {
    Try(toMonitorContendedEnteredEvent)
  }

  /**
   * Returns the event as a monitor contended entered event.
   *
   * @return The monitor contended entered event profile wrapping the event
   * @throws AssertionError If not a monitor contended entered event
   */
  @throws[AssertionError]
  def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfo

  /**
   * Returns the event as a monitor contended enter event.
   *
   * @return Success containing the monitor contended enter event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMonitorContendedEnterEvent: Try[MonitorContendedEnterEventInfo] = {
    Try(toMonitorContendedEnterEvent)
  }

  /**
   * Returns the event as a monitor contended enter event.
   *
   * @return The monitor contended enter event profile wrapping the event
   * @throws AssertionError If not a monitor contended enter event
   */
  @throws[AssertionError]
  def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfo

  /**
   * Returns the event as a monitor waited event.
   *
   * @return Success containing the monitor waited event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMonitorWaitedEvent: Try[MonitorWaitedEventInfo] = {
    Try(toMonitorWaitedEvent)
  }

  /**
   * Returns the event as a monitor waited event.
   *
   * @return The monitor waited event profile wrapping the event
   * @throws AssertionError If not a monitor waited event
   */
  @throws[AssertionError]
  def toMonitorWaitedEvent: MonitorWaitedEventInfo

  /**
   * Returns the event as a monitor wait event.
   *
   * @return Success containing the monitor wait event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToMonitorWaitEvent: Try[MonitorWaitEventInfo] = {
    Try(toMonitorWaitEvent)
  }

  /**
   * Returns the event as a monitor wait event.
   *
   * @return The monitor wait event profile wrapping the event
   * @throws AssertionError If not a monitor wait event
   */
  @throws[AssertionError]
  def toMonitorWaitEvent: MonitorWaitEventInfo

  /**
   * Returns the event as a step event.
   *
   * @return Success containing the step event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToStepEvent: Try[StepEventInfo] = {
    Try(toStepEvent)
  }

  /**
   * Returns the event as a step event.
   *
   * @return The step event profile wrapping the event
   * @throws AssertionError If not a step event
   */
  @throws[AssertionError]
  def toStepEvent: StepEventInfo

  /**
   * Returns the event as a thread death event.
   *
   * @return Success containing the thread death event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToThreadDeathEvent: Try[ThreadDeathEventInfo] = {
    Try(toThreadDeathEvent)
  }

  /**
   * Returns the event as a thread death event.
   *
   * @return The thread death event profile wrapping the event
   * @throws AssertionError If not a thread death event
   */
  @throws[AssertionError]
  def toThreadDeathEvent: ThreadDeathEventInfo

  /**
   * Returns the event as a thread start event.
   *
   * @return Success containing the thread start event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToThreadStartEvent: Try[ThreadStartEventInfo] = {
    Try(toThreadStartEvent)
  }

  /**
   * Returns the event as a thread start event.
   *
   * @return The thread start event profile wrapping the event
   * @throws AssertionError If not a thread start event
   */
  @throws[AssertionError]
  def toThreadStartEvent: ThreadStartEventInfo

  /**
   * Returns the event as a vm death event.
   *
   * @return Success containing the vm death event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToVMDeathEvent: Try[VMDeathEventInfo] = {
    Try(toVMDeathEvent)
  }

  /**
   * Returns the event as a vm death event.
   *
   * @return The vm death event profile wrapping the event
   * @throws AssertionError If not a vm death event
   */
  @throws[AssertionError]
  def toVMDeathEvent: VMDeathEventInfo

  /**
   * Returns the event as a vm disconnect event.
   *
   * @return Success containing the vm disconnect event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToVMDisconnectEvent: Try[VMDisconnectEventInfo] = {
    Try(toVMDisconnectEvent)
  }

  /**
   * Returns the event as a vm disconnect event.
   *
   * @return The vm disconnect event profile wrapping the event
   * @throws AssertionError If not a vm disconnect event
   */
  @throws[AssertionError]
  def toVMDisconnectEvent: VMDisconnectEventInfo

  /**
   * Returns the event as a vm start event.
   *
   * @return Success containing the vm start event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToVMStartEvent: Try[VMStartEventInfo] = {
    Try(toVMStartEvent)
  }

  /**
   * Returns the event as a vm start event.
   *
   * @return The vm start event profile wrapping the event
   * @throws AssertionError If not a vm start event
   */
  @throws[AssertionError]
  def toVMStartEvent: VMStartEventInfo

  /**
   * Returns the event as a watchpoint event.
   *
   * @return Success containing the watchpoint event profile wrapping
   *         the event, otherwise a failure
   */
  def tryToWatchpointEvent: Try[WatchpointEventInfo] = {
    Try(toWatchpointEvent)
  }

  /**
   * Returns the event as a watchpoint event.
   *
   * @return The watchpoint event profile wrapping the event
   * @throws AssertionError If not a watchpoint event
   */
  @throws[AssertionError]
  def toWatchpointEvent: WatchpointEventInfo

  /**
   * Returns the event converted to its most specific event profile.
   * For example, if the current profile is of type 'EventInfo'
   * and the most specific possible was 'BreakpointEventProfile', a
   * new instance of 'BreakpointEventProfile' would be returned.
   *
   * @return The new, specific event profile instance
   */
  def toMostSpecificEvent: EventInfo = {
    if (this.isAccessWatchpointEvent)
      this.toAccessWatchpointEvent
    else if (this.isBreakpointEvent)
      this.toBreakpointEvent
    else if (this.isClassPrepareEvent)
      this.toClassPrepareEvent
    else if (this.isClassUnloadEvent)
      this.toClassUnloadEvent
    else if (this.isExceptionEvent)
      this.toExceptionEvent
    else if (this.isMethodEntryEvent)
      this.toMethodEntryEvent
    else if (this.isMethodExitEvent)
      this.toMethodExitEvent
    else if (this.isModificationWatchpointEvent)
      this.toModificationWatchpointEvent
    else if (this.isMonitorContendedEnteredEvent)
      this.toMonitorContendedEnteredEvent
    else if (this.isMonitorContendedEnterEvent)
      this.toMonitorContendedEnterEvent
    else if (this.isMonitorWaitedEvent)
      this.toMonitorWaitedEvent
    else if (this.isMonitorWaitEvent)
      this.toMonitorWaitEvent
    else if (this.isStepEvent)
      this.toStepEvent
    else if (this.isThreadDeathEvent)
      this.toThreadDeathEvent
    else if (this.isThreadStartEvent)
      this.toThreadStartEvent
    else if (this.isVMDeathEvent)
      this.toVMDeathEvent
    else if (this.isVMDisconnectEvent)
      this.toVMDisconnectEvent
    else if (this.isVMStartEvent)
      this.toVMStartEvent
    else if (this.isMonitorEvent)
      this.toMonitorEvent
    else if (this.isWatchpointEvent)
      this.toWatchpointEvent
    else if (this.isLocatableEvent)
      this.toLocatableEvent
    else
      this
  }

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
  def isPlainEvent: Boolean

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    Try {
      val specificEvent = this.toMostSpecificEvent

      if (specificEvent.isPlainEvent) specificEvent.toString
      else specificEvent.toPrettyString
    }.getOrElse("<ERROR>")
  }
}
