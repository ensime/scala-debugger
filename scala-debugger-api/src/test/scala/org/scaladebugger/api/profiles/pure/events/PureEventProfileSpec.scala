package org.scaladebugger.api.profiles.pure.events
import acyclic.file

import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.{PendingEventHandlerSupportLike, EventHandlerInfo, EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import test.JDIMockHelpers

import scala.util.Success

class PureEventProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestEventHandlerId = java.util.UUID.randomUUID().toString
  private val mockEventManager = mock[EventManager]
  private val pureEventProfile = new Object with PureEventProfile {
    override protected val eventManager = mockEventManager
  }

  describe("PureEventProfile") {
    describe("#eventHandlers") {
      it("should include all active requests") {
        val expected = Seq(
          EventHandlerInfo(TestEventHandlerId, mock[EventType], mock[EventHandler])
        )

        val mockEventManager = mock[PendingEventHandlerSupportLike]
        val pureEventProfile = new Object with PureEventProfile {
          override protected val eventManager: EventManager = mockEventManager
        }

        (mockEventManager.getAllEventHandlerInfo _).expects()
          .returning(expected).once()

        (mockEventManager.pendingEventHandlers _).expects()
          .returning(Nil).once()

        val actual = pureEventProfile.eventHandlers

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          EventHandlerInfo(TestEventHandlerId, mock[EventType], mock[EventHandler])
        )

        val mockEventManager = mock[PendingEventHandlerSupportLike]
        val pureEventProfile = new Object with PureEventProfile {
          override protected val eventManager: EventManager = mockEventManager
        }

        (mockEventManager.getAllEventHandlerInfo _).expects()
          .returning(Nil).once()

        (mockEventManager.pendingEventHandlers _).expects()
          .returning(expected).once()

        val actual = pureEventProfile.eventHandlers

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          EventHandlerInfo(TestEventHandlerId, mock[EventType], mock[EventHandler])
        )

        (mockEventManager.getAllEventHandlerInfo _).expects()
          .returning(expected).once()

        val actual = pureEventProfile.eventHandlers

        actual should be (expected)
      }
    }

    describe("#tryCreateEventListenerWithData") {
      it("should set a low-level event and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[PureEventProfile#EventAndData]
        ))
        val eventType = stub[EventType] // Using mock throws stack overflow
        val requestArguments = Seq(mock[JDIRequestArgument])
        val eventArguments = Seq(mock[JDIEventArgument])
        val arguments = requestArguments ++ eventArguments

        (mockEventManager.addEventDataStream _)
          .expects(eventType, eventArguments)
          .returning(expected.get).once()

        val actual = pureEventProfile.tryCreateEventListenerWithData(
          eventType,
          arguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
