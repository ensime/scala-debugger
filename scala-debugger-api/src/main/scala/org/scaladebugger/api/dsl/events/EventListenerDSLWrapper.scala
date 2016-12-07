package org.scaladebugger.api.dsl.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerRequest
import org.scaladebugger.api.profiles.traits.info.events.EventInfo

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param eventListenerProfile The profile to wrap
 */
class EventListenerDSLWrapper private[dsl] (
  private val eventListenerProfile: EventListenerRequest
) {
  /** Represents a Event event and any associated data. */
  type EventEventAndData = (EventInfo, Seq[JDIEventDataResult])

  /** @see EventListenerRequest#tryCreateEventListener(EventType, JDIArgument*) */
  def onEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventInfo]] =
    eventListenerProfile.tryCreateEventListener(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerRequest#createEventListener(EventType, JDIArgument*) */
  def onUnsafeEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventInfo] =
    eventListenerProfile.createEventListener(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerRequest#createEventListenerWithData(EventType, JDIArgument*) */
  def onUnsafeEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventEventAndData] =
    eventListenerProfile.createEventListenerWithData(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerRequest#tryCreateEventListenerWithData(EventType, JDIArgument*) */
  def onEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventEventAndData]] =
    eventListenerProfile.tryCreateEventListenerWithData(
      eventType,
      extraArguments: _*
    )
}
