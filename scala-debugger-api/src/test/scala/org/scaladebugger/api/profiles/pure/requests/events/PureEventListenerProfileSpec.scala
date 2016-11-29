package org.scaladebugger.api.profiles.pure.requests.events
import acyclic.file
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventHandlerInfo, EventManager, JDIEventArgument, PendingEventHandlerSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.JDIMockHelpers

import scala.util.Success

class PureEventListenerProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestEventHandlerId = java.util.UUID.randomUUID().toString
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val pureEventListenerProfile = new Object with PureEventListenerProfile {
    override protected val eventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureEventListenerProfile") {
    describe("#eventHandlers") {
      it("should include all active requests") {
        val expected = Seq(
          EventHandlerInfo(TestEventHandlerId, mock[EventType], mock[EventHandler])
        )

        val mockEventManager = mock[PendingEventHandlerSupportLike]
        val pureEventProfile = new Object with PureEventListenerProfile {
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
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
        val pureEventProfile = new Object with PureEventListenerProfile {
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
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

        val actual = pureEventListenerProfile.eventHandlers

        actual should be (expected)
      }
    }

    describe("#tryCreateEventListenerWithData") {
      it("should set a low-level event and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[PureEventListenerProfile#EventAndData]
        ))
        val eventType = stub[EventType] // Using mock throws stack overflow
        val requestArguments = Seq(mock[JDIRequestArgument])
        val eventArguments = Seq(mock[JDIEventArgument])
        val arguments = requestArguments ++ eventArguments

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        (mockEventManager.addEventDataStream _)
          .expects(eventType, eventArguments)
          .returning(lowlevelPipeline).once()

        val actual = pureEventListenerProfile.tryCreateEventListenerWithData(
          eventType,
          arguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
