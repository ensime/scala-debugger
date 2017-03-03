package org.scaladebugger.api.profiles.java.requests.events

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.{EventHandlerInfo, EventManager, PendingEventHandlerSupportLike}
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.events.EventListenerRequest
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java profile for events that adds no extra logic on
 * top of the standard JDI.
 */
trait JavaEventListenerRequest extends EventListenerRequest {
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryCreateEventListenerWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    eventManager.addEventDataStream(eventType, eArgs: _*)
      .map(t => (eventProducer.newDefaultEventInfo(
        scalaVirtualMachine, t._1, rArgs ++ eArgs: _*
      ), t._2))
      .noop()
  }

  /**
   * Retrieves the collection of active event handlers.
   *
   * @return The collection of information on event handlers
   */
  override def eventHandlers: Seq[EventHandlerInfo] = {
    eventManager.getAllEventHandlerInfo ++ (eventManager match {
      case p: PendingEventHandlerSupportLike  => p.pendingEventHandlers
      case _                                  => Nil
    })
  }
}
