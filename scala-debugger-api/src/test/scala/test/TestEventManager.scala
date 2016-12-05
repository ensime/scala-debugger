package test

import org.scaladebugger.api.lowlevel.events.{EventHandlerInfo, JDIEventArgument, EventManager}
import org.scaladebugger.api.lowlevel.events.EventType.EventType

/**
 * Test event manager that merely invokes the provided event manager
 * underneath to make it easier to mock.
 *
 * @param eventManager The underlying event manager used to execute
 *                          all methods
 */
class TestEventManager(
  private val eventManager: EventManager
) extends EventManager {
  override def addEventHandlerWithId(eventHandlerId: String, eventType: EventType, eventHandler: EventHandler, eventArguments: JDIEventArgument*): String =
    eventManager.addEventHandlerWithId(eventHandlerId, eventType, eventHandler, eventArguments: _*)
  override def getHandlersForEventType(eventType: EventType): Seq[EventHandler] =
    eventManager.getHandlersForEventType(eventType)
  override def stop(): Unit =
    eventManager.stop()
  override def removeEventHandler(eventHandlerId: String): Option[EventHandler] =
    eventManager.removeEventHandler(eventHandlerId)
  override def isRunning: Boolean =
    eventManager.isRunning
  override def getHandlerIdsForEventType(eventType: EventType): Seq[String] =
  eventManager.getHandlerIdsForEventType(eventType)
  override def getEventHandler(eventHandlerId: String): Option[EventHandler] =
    eventManager.getEventHandler(eventHandlerId)
  override def start(): Unit =
    eventManager.start()
  override def getAllEventHandlerInfo: Seq[EventHandlerInfo] =
    eventManager.getAllEventHandlerInfo
}
