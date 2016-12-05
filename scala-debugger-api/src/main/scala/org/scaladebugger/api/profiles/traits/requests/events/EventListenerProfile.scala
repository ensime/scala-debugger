package org.scaladebugger.api.profiles.traits.requests.events

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventHandlerInfo
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * event listener functionality for a specific debug profile.
 */
trait EventListenerProfile {
  /** Represents an event and any associated data. */
  type EventAndData = (EventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active event handlers.
   *
   * @return The collection of information on event handlers
   */
  def eventHandlers: Seq[EventHandlerInfo]

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events
   */
  def tryCreateEventListener(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventInfoProfile]] = {
    tryCreateEventListenerWithData(eventType, extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events
   */
  def createEventListener(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventInfoProfile] = {
    tryCreateEventListener(eventType, extraArguments: _*).get
  }

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  def createEventListenerWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventAndData] = {
    tryCreateEventListenerWithData(eventType, extraArguments: _*).get
  }

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryCreateEventListenerWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventAndData]]
}
