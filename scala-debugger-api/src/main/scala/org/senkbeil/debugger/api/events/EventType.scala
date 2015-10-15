package org.senkbeil.debugger.api.events

import com.sun.jdi.event._

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
      case _: VMStartEvent                => VMStartEventType
      case _: VMDisconnectEvent           => VMDisconnectEventType
      case _: VMDeathEvent                => VMDeathEventType
      case _: ThreadStartEvent            => ThreadStartEventType
      case _: ThreadDeathEvent            => ThreadDeathEventType
      case _: ClassPrepareEvent           => ClassPrepareEventType
      case _: ClassUnloadEvent            => ClassUnloadEventType
      case _: AccessWatchpointEvent       => AccessWatchpointEventType
      case _: ModificationWatchpointEvent => ModificationWatchpointEventType
      case _: ExceptionEvent              => ExceptionEventType
      case _: MethodEntryEvent            => MethodEntryEventType
      case _: MethodExitEvent             => MethodExitEventType
      case _: BreakpointEvent             => BreakpointEventType
      case _: StepEvent                   => StepEventType
      case _                              => null
    })
  }
}

