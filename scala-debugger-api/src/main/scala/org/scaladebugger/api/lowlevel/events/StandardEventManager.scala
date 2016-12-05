package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.misc.Resume
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner, MultiMap}
import com.sun.jdi.event.{Event, EventQueue, EventSet}
import EventType._

import scala.util.{Failure, Success, Try}

/**
 * Represents a manager for events coming in from a virtual machine.
 *
 * @param eventQueue The event queue whose events to pull off and process
 * @param loopingTaskRunner The runner used to process events
 * @param autoStart If true, starts the event processing automatically
 * @param onExceptionResume If true, any event handler that throws an exception
 *                          will count towards resuming the event set, otherwise
 *                          it will cause the event set to not resume
 */
class StandardEventManager(
  private val eventQueue: EventQueue,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val autoStart: Boolean = true,
  private val onExceptionResume: Boolean = true
) extends EventManager with Logging {
  /** Contains the events, associated handlers and their ids. */
  private val eventHandlers = new MultiMap[EventType, EventHandlerInfo]

  private var eventTaskId: Option[String] = None

  /**
   * Indicates whether or not the event manager is processing events.
   *
   * @return True if it is running, otherwise false
   */
  override def isRunning: Boolean = eventTaskId.nonEmpty

  /**
   * Begins the processing of events from the virtual machine.
   */
  override def start(): Unit = {
    assert(!isRunning, "Event manager already started!")

    logger.trace("Starting event manager for virtual machine!")
    eventTaskId = Some(loopingTaskRunner.addTask(eventHandlerTask()))

    eventTaskId.foreach(id =>
      logger.trace(s"Event process task: $id"))
  }

  /**
   * Represents the task to be added per event handler. Can be overridden to
   * perform different tasks.
   */
  protected def eventHandlerTask(): Unit = {
    val eventSet = eventQueue.remove()

    // Process the set of events, returning whether or not the event
    // set should resume
    val eventSetProcessor = newEventSetProcessor(eventSet)
    eventSetProcessor.process()
  }

  /**
   * Creates a new event set processor. Can be overridden.
   *
   * @param eventSet The event set to process
   * @return The new event set processor instance
   */
  protected def newEventSetProcessor(
    eventSet: EventSet
  ): EventSetProcessor = new EventSetProcessor(
    eventSet                = eventSet,
    eventFunctionRetrieval  = getHandlersForEventType,
    onExceptionResume       = onExceptionResume
  )

  /**
   * Ends the processing of events from the virtual machine.
   */
  override def stop(): Unit = {
    assert(isRunning, "Event manager not started!")

    logger.trace(s"Stopping event manager ($eventTaskId) for virtual machine!")
    loopingTaskRunner.removeTask(eventTaskId.get)
    eventTaskId = None
  }

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
   * @return The id associated with the event handler
   */
  override def addEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String = {
    logger.trace(s"Created event handler with id '$eventHandlerId'")
    wrapAndAddEventHandler(
      eventHandlerId,
      eventType,
      eventHandler,
      eventArguments: _*
    )
  }

  /**
   * Wraps the provided event handler and adds it to the internal collection.
   *
   * @param eventHandlerId The id to associate with the event handler
   * @param eventType The type of the event to match against the handler
   * @param eventHandler The event handler function to be wrapped
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   * @return The id associated with the wrapped event handler
   */
  protected def wrapAndAddEventHandler(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String = {
    // Create a wrapper that contains our filtering logic
    val wrapperEventHandler =
      newWrapperEventHandler(eventHandler, eventArguments)

    // Store the event handler with the filtering logic
    eventHandlers.putWithId(
      eventHandlerId,
      eventType,
      EventHandlerInfo(
        eventHandlerId,
        eventType,
        wrapperEventHandler,
        eventArguments
      )
    )

    eventHandlerId
  }

  /**
   * Generates a wrapper function around the event handler, using an argument
   * processor to evaluate the provided arguments to determine whether or not
   * to invoke the event handler as well as retrieve any requested data.
   *
   * @param eventHandler The event handler to wrap
   * @param eventArguments The arguments to use when determining if the event
   *                       handler should be invoked and what data to be
   *                       retrieved
   * @return The wrapper around the event handler
   */
  protected def newWrapperEventHandler(
    eventHandler: EventHandler,
    eventArguments: Seq[JDIEventArgument]
  ): EventHandler = {
    val jdiEventArgumentProcessor =
      new JDIEventArgumentProcessor(eventArguments: _*)

    // Create a wrapper function that invokes the event handler only if the
    // filter processor yields a positive result, otherwise skip this handler
    (event: Event, data: Seq[JDIEventDataResult]) => {
      val resumeFlag = eventArguments.collect {
        case r: Resume => r
      }.lastOption.map(_.value)

      Try(jdiEventArgumentProcessor.processAll(event)) match {
        case Success(result) =>
          val (passesFilters, data, _) = result

          if (passesFilters) {
            val result = eventHandler(event, data)
            resumeFlag.getOrElse(result)
          } else true

        case Failure(throwable) =>
          logger.error(s"Failed to process event $event", throwable)
          true
      }
    }
  }

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   * @return The collection of event functions
   */
  override def getHandlersForEventType(
    eventType: EventType
  ) : Seq[EventHandler] = {
    eventHandlers.get(eventType).map(_.map(_.eventHandler)).getOrElse(Nil)
  }

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   * @return The collection of event functions
   */
  override def getHandlerIdsForEventType(eventType: EventType): Seq[String] = {
    eventHandlers.getIdsWithKey(eventType).getOrElse(Nil)
  }

  /**
   * Retrieves the handler with the specified id.
   *
   * @param id The id of the handler to retrieve
   * @return Some event handler if found, otherwise None
   */
  override def getEventHandler(id: String): Option[EventHandler] = {
    eventHandlers.getWithId(id).map(_.eventHandler)
  }

  /**
   * Retrieves information on all event handlers.
   *
   * @return The collection of information on all event handlers
   */
  override def getAllEventHandlerInfo: Seq[EventHandlerInfo] = {
    eventHandlers.values
  }

  /**
   * Removes the event function from this manager.
   *
   * @param id The id of the event handler to remove
   * @return Some event handler if removed, otherwise None
   */
  override def removeEventHandler(id: String): Option[EventHandler] = {
    eventHandlers.removeWithId(id).map(_.eventHandler)
  }

  // ==========================================================================
  // = CONSTRUCTOR
  // ==========================================================================

  // If marked to start automatically, do so
  if (autoStart) start()
}
