package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event._

import scala.reflect.ClassTag

/**
 * Represents available event types to be processed by the event manager.
 */
object EventType extends Enumeration {
  type EventType = Value
  val VMStartEventType,
      VMDisconnectEventType,
      VMDeathEventType,
      ThreadStartEventType,
      ThreadDeathEventType,
      ClassPrepareEventType,
      ClassUnloadEventType,
      AccessWatchpointEventType,
      ModificationWatchpointEventType,
      MonitorContendedEnteredEventType,
      MonitorContendedEnterEventType,
      MonitorWaitedEventType,
      MonitorWaitEventType,
      ExceptionEventType,
      MethodEntryEventType,
      MethodExitEventType,
      BreakpointEventType,
      StepEventType = Value

  /**
   * Converts a JDI event to the equivalent event type.
   *
   * @param event The event to convert
   *
   * @return Some event type if the event has a corresponding event type,
   *         otherwise None
   */
  def eventToEventType(event: Event): Option[EventType] = {
    // NOTE: Small reminder that Option.apply(null) returns None in this case
    Option(event match {
      case _: VMStartEvent                  => VMStartEventType
      case _: VMDisconnectEvent             => VMDisconnectEventType
      case _: VMDeathEvent                  => VMDeathEventType
      case _: ThreadStartEvent              => ThreadStartEventType
      case _: ThreadDeathEvent              => ThreadDeathEventType
      case _: ClassPrepareEvent             => ClassPrepareEventType
      case _: ClassUnloadEvent              => ClassUnloadEventType
      case _: AccessWatchpointEvent         => AccessWatchpointEventType
      case _: ModificationWatchpointEvent   => ModificationWatchpointEventType
      case _: MonitorContendedEnteredEvent  => MonitorContendedEnteredEventType
      case _: MonitorContendedEnterEvent    => MonitorContendedEnterEventType
      case _: MonitorWaitedEvent            => MonitorWaitedEventType
      case _: MonitorWaitEvent              => MonitorWaitEventType
      case _: ExceptionEvent                => ExceptionEventType
      case _: MethodEntryEvent              => MethodEntryEventType
      case _: MethodExitEvent               => MethodExitEventType
      case _: BreakpointEvent               => BreakpointEventType
      case _: StepEvent                     => StepEventType
      case _                                => null
    })
  }

  /**
   * Converts a JDI event class to the equivalent event type.
   *
   * @param eventClass The class of the event to convert
   *
   * @return Some event type if the event has a corresponding event type,
   *         otherwise None
   */
  def eventClassToEventType[A <: Event : ClassTag](
    eventClass: Class[A]
  ): Option[EventType] = {
    // TODO: Determine if there is some way to pattern match against type
    Option(
      if (eventClass == classOf[VMStartEvent]) VMStartEventType
      else if (eventClass == classOf[VMDisconnectEvent]) VMDisconnectEventType
      else if (eventClass == classOf[VMDeathEvent]) VMDeathEventType
      else if (eventClass == classOf[ThreadStartEvent]) ThreadStartEventType
      else if (eventClass == classOf[ThreadDeathEvent]) ThreadDeathEventType
      else if (eventClass == classOf[ClassPrepareEvent]) ClassPrepareEventType
      else if (eventClass == classOf[ClassUnloadEvent]) ClassUnloadEventType
      else if (eventClass == classOf[AccessWatchpointEvent]) AccessWatchpointEventType
      else if (eventClass == classOf[ModificationWatchpointEvent]) ModificationWatchpointEventType
      else if (eventClass == classOf[MonitorContendedEnteredEvent]) MonitorContendedEnteredEventType
      else if (eventClass == classOf[MonitorContendedEnterEvent]) MonitorContendedEnterEventType
      else if (eventClass == classOf[MonitorWaitedEvent]) MonitorWaitedEventType
      else if (eventClass == classOf[MonitorWaitEvent]) MonitorWaitEventType
      else if (eventClass == classOf[ExceptionEvent]) ExceptionEventType
      else if (eventClass == classOf[MethodEntryEvent]) MethodEntryEventType
      else if (eventClass == classOf[MethodExitEvent]) MethodExitEventType
      else if (eventClass == classOf[BreakpointEvent]) BreakpointEventType
      else if (eventClass == classOf[StepEvent]) StepEventType
      else null
    )
  }
}

