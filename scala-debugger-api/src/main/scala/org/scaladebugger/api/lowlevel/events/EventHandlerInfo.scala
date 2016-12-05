package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scaladebugger.api.lowlevel.events.EventType.EventType

/**
 * Represents information about an event handler.
 *
 * @param eventHandlerId The id associated with the event handler
 * @param eventType The type of event with which the event handler is associated
 * @param eventHandler The function representing the event handler
 * @param extraArguments Any additional arguments associated with the handler
 */
case class EventHandlerInfo(
  eventHandlerId: String,
  eventType: EventType,
  eventHandler: EventHandler,
  extraArguments: Seq[JDIEventArgument] = Nil
)
