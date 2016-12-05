package org.scaladebugger.api.lowlevel.events

import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.utils.PendingActionManager

/**
 * Provides pending event capabilities to an existing event manager. Note that
 * all newly-created event handlers will be pending while pending is enabled.
 */
trait PendingEventHandlerSupport extends PendingEventHandlerSupportLike {
  /**
   * Represents the manager used to store pending event handlers and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[EventHandlerInfo]

  /**
   * Processes all pending event handlers.
   *
   * @return The collection of successfully-processed event handlers
   */
  override def processAllPendingEventHandlers(): Seq[EventHandlerInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of all pending event handlers.
   *
   * @return The collection of event request information
   */
  override def pendingEventHandlers: Seq[EventHandlerInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
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
   *
   * @return The id associated with the event handler
   */
  abstract override def addEventHandlerWithId(
    eventHandlerId: String,
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String = {
    def createEventHandler() = super.addEventHandlerWithId(
      eventHandlerId,
      eventType,
      eventHandler,
      eventArguments: _*
    )

    // Pending is an all-or-nothing operation where we will always set to
    // pending when enabled and never set to pending when disabled
    if (isPendingSupportEnabled) {
      pendingActionManager.addPendingAction(
        EventHandlerInfo(
          eventHandlerId,
          eventType,
          eventHandler,
          eventArguments
        ),
        () => createEventHandler()
      )
    } else {
      createEventHandler()
    }

    eventHandlerId
  }
}
