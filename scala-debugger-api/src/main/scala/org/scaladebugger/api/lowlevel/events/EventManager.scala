package org.scaladebugger.api.lowlevel.events
//import acyclic.file

import com.sun.jdi.event.{Event, EventQueue, EventSet}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner, MultiMap}

/**
 * Contains public types related to the event manager.
 */
object EventManager {
  /**
   * Represents an event callback, receiving the event and returning whether or
   * not to resume.
   */
  type EventHandler = (Event, Seq[JDIEventDataResult]) => Boolean

  /**
   * Represents a JDI event and any associated data retrieved from it.
   */
  type EventAndData = (Event, Seq[JDIEventDataResult])

  /**
   * Represents the field in pipelines that contains the id of the event handler
   * that is used to feed the event pipelines.
   */
  val EventHandlerIdMetadataField = "event-handler-id"
}

/**
 * Represents a manager for events coming in from a virtual machine.
 */
trait EventManager {
  /**
   * Represents an event callback, receiving the event and returning whether or
   * not to resume.
   */
  type EventHandler = EventManager.EventHandler

  /**
   * Represents a JDI event and any associated data retrieved from it.
   */
  type EventAndData = EventManager.EventAndData

  /**
   * Indicates whether or not the event manager is processing events.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean

  /**
   * Begins the processing of events from the virtual machine.
   */
  def start(): Unit

  /**
   * Ends the processing of events from the virtual machine.
   */
  def stop(): Unit

  /**
   * Adds a new event stream that tracks incoming JDI events.
   *
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   */
  def addEventStream(
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[Event] = addEventStreamWithId(
    newEventId(),
    eventType,
    eventArguments: _*
  )

  /**
   * Adds a new event stream that tracks incoming JDI events.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   */
  def addEventStreamWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[Event] = {
    addEventDataStreamWithId(eventHandlerId, eventType, eventArguments: _*)
      .map(_._1).noop()
  }

  /**
   * Adds a new event data stream that tracks incoming JDI events and data
   * collected from those events.
   *
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   *         and collected data
   */
  def addEventDataStream(
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[EventAndData] = addEventDataStreamWithId(
    newEventId(),
    eventType,
    eventArguments: _*
  )

  /**
   * Adds a new event data stream that tracks incoming JDI events and data
   * collected from those events.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   *         and collected data
   */
  def addEventDataStreamWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[EventAndData] = {
    val eventPipeline = Pipeline.newPipeline(
      classOf[EventAndData],
      () => removeEventHandler(eventHandlerId),
      Map(EventManager.EventHandlerIdMetadataField -> eventHandlerId)
    )

    // Create a resuming event handler while providing our own id
    addResumingEventHandlerWithId(
      eventHandlerId,
      eventType,
      (e, d) => eventPipeline.process((e, d)),
      eventArguments: _*
    )

    eventPipeline
  }

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
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
  def addResumingEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: (Event, Seq[JDIEventDataResult]) => Unit,
    eventArguments: JDIEventArgument*
  ): String = {
    // Convert the function to an "always true" event function
    val fullEventFunction = (event: Event, data: Seq[JDIEventDataResult]) => {
      eventHandler(event, data)
      true
    }

    addEventHandlerWithId(
      eventHandlerId,
      eventType,
      fullEventFunction,
      eventArguments: _*
    )
  }

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event and
   *                     a collection of retrieved data from the event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addResumingEventHandler(
    eventType: EventType,
    eventHandler: (Event, Seq[JDIEventDataResult]) => Unit,
    eventArguments: JDIEventArgument*
  ): String = addResumingEventHandlerWithId(
    newEventId(),
    eventType,
    eventHandler,
    eventArguments: _*
  )

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addResumingEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: (Event) => Unit,
    eventArguments: JDIEventArgument*
  ): String = addResumingEventHandlerWithId(
    eventHandlerId = eventHandlerId,
    eventType = eventType,
    eventHandler = (event: Event, _: Seq[JDIEventDataResult]) => {
      eventHandler(event)
    },
    eventArguments: _*
  )

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addResumingEventHandler(
    eventType: EventType,
    eventHandler: (Event) => Unit,
    eventArguments: JDIEventArgument*
  ): String = addResumingEventHandlerWithId(
    newEventId(),
    eventType,
    eventHandler,
    eventArguments: _*
  )

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
  def addEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event and
   *                     a collection of retrieved data from the event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addEventHandler(
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String = addEventHandlerWithId(
    newEventId(),
    eventType,
    eventHandler,
    eventArguments: _*
  )

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: (Event) => Boolean,
    eventArguments: JDIEventArgument*
  ): String = addEventHandlerWithId(
    eventHandlerId = eventHandlerId,
    eventType = eventType,
    eventHandler = (event: Event, _: Seq[JDIEventDataResult]) => {
      eventHandler(event)
    },
    eventArguments: _*
  )

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addEventHandler(
    eventType: EventType,
    eventHandler: (Event) => Boolean,
    eventArguments: JDIEventArgument*
  ): String = addEventHandlerWithId(
    newEventId(),
    eventType,
    eventHandler,
    eventArguments: _*
  )

  /**
   * Adds an event handler based on the specified information.
   *
   * @param eventHandlerInfo The information used to add the event hander
   *
   * @return The id associated with the event handler
   */
  def addEventHandlerFromInfo(
    eventHandlerInfo: EventHandlerInfo
  ): String = addEventHandlerWithId(
    eventHandlerInfo.eventHandlerId,
    eventHandlerInfo.eventType,
    eventHandlerInfo.eventHandler,
    eventHandlerInfo.extraArguments: _*
  )

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  def getHandlersForEventType(eventType: EventType) : Seq[EventHandler]

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  def getHandlerIdsForEventType(eventType: EventType): Seq[String]

  /**
   * Retrieves the handler with the specified id.
   *
   * @param eventHandlerId The id of the handler to retrieve
   *
   * @return Some event handler if found, otherwise None
   */
  def getEventHandler(eventHandlerId: String): Option[EventHandler]

  /**
   * Retrieves information on all event handlers.
   *
   * @return The collection of information on all event handlers
   */
  def getAllEventHandlerInfo: Seq[EventHandlerInfo]

  /**
   * Removes the event function from this manager.
   *
   * @param eventHandlerId The id of the event handler to remove
   *
   * @return Some event handler if removed, otherwise None
   */
  def removeEventHandler(eventHandlerId: String): Option[EventHandler]

  /**
   * Generates an id for a new event handler.
   *
   * @return The id as a string
   */
  protected def newEventId(): String =
    java.util.UUID.randomUUID().toString
}
