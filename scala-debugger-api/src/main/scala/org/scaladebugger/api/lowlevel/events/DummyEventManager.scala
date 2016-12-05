package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.lowlevel.events.EventType.EventType

/**
 * Represents an event manager whose operations do nothing.
 */
class DummyEventManager extends EventManager {
  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event and
   *                     a collection of retrieved data from the event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  override def addEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String = eventHandlerId

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  override def getHandlersForEventType(
    eventType: EventType
  ): Seq[EventHandler] = Nil

  /**
   * Ends the processing of events from the virtual machine.
   */
  override def stop(): Unit = {}

  /**
   * Removes the event function from this manager.
   *
   * @param eventHandlerId The id of the event handler to remove
   *
   * @return Some event handler if removed, otherwise None
   */
  override def removeEventHandler(
    eventHandlerId: String
  ): Option[EventHandler] = None

  /**
   * Indicates whether or not the event manager is processing events.
   *
   * @return True if it is running, otherwise false
   */
  override def isRunning: Boolean = false

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  override def getHandlerIdsForEventType(
    eventType: EventType
  ): Seq[String] = Nil

  /**
   * Retrieves the handler with the specified id.
   *
   * @param eventHandlerId The id of the handler to retrieve
   *
   * @return Some event handler if found, otherwise None
   */
  override def getEventHandler(
    eventHandlerId: String
  ): Option[EventHandler] = None

  /**
   * Begins the processing of events from the virtual machine.
   */
  override def start(): Unit = {}

  /**
   * Retrieves information on all event handlers.
   *
   * @return The collection of information on all event handlers
   */
  override def getAllEventHandlerInfo: Seq[EventHandlerInfo] = Nil
}
