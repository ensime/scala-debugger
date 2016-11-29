package org.scaladebugger.api.dsl.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerProfile
import org.scaladebugger.api.profiles.traits.info.events.EventInfoProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param eventListenerProfile The profile to wrap
 */
class EventListenerDSLWrapper private[dsl] (
  private val eventListenerProfile: EventListenerProfile
) {
  /** Represents a Event event and any associated data. */
  type EventEventAndData = (EventInfoProfile, Seq[JDIEventDataResult])

  /** @see EventListenerProfile#tryCreateEventListener(EventType, JDIArgument*) */
  def onEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventInfoProfile]] =
    eventListenerProfile.tryCreateEventListener(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerProfile#createEventListener(EventType, JDIArgument*) */
  def onUnsafeEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventInfoProfile] =
    eventListenerProfile.createEventListener(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerProfile#createEventListenerWithData(EventType, JDIArgument*) */
  def onUnsafeEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventEventAndData] =
    eventListenerProfile.createEventListenerWithData(
      eventType,
      extraArguments: _*
    )

  /** @see EventListenerProfile#tryCreateEventListenerWithData(EventType, JDIArgument*) */
  def onEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventEventAndData]] =
    eventListenerProfile.tryCreateEventListenerWithData(
      eventType,
      extraArguments: _*
    )
}
