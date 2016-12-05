package org.scaladebugger.api.profiles.swappable.requests.events

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventHandlerInfo
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerProfile

import scala.util.Try

/**
 * Represents a swappable profile for events that redirects the
 * invocation to another profile.
 */
trait SwappableEventListenerProfile extends EventListenerProfile {
  this: SwappableDebugProfileManagement =>

  override def tryCreateEventListenerWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventAndData]] = {
    withCurrentProfile.tryCreateEventListenerWithData(eventType, extraArguments: _*)
  }

  override def eventHandlers: Seq[EventHandlerInfo] = {
    withCurrentProfile.eventHandlers
  }
}
