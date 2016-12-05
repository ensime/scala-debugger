package org.scaladebugger.api.lowlevel.events

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestEventManager}

class PendingEventHandlerSupportSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestHandlerId = java.util.UUID.randomUUID().toString
  private val mockEventManager = mock[EventManager]

  private class TestEventInfoPendingActionManager
    extends PendingActionManager[EventHandlerInfo]
  private val mockPendingActionManager =
    mock[TestEventInfoPendingActionManager]

  private val pendingEventHandlerSupport = new TestEventManager(
    mockEventManager
  ) with PendingEventHandlerSupport {
    override protected def newEventId(): String = TestHandlerId

    override protected val pendingActionManager: PendingActionManager[EventHandlerInfo] =
      mockPendingActionManager
  }

  describe("PendingEventHandlerSupport") {
    describe("#processAllPendingEventHandlers") {
      it("should process all pending eventHandlers") {
        val testEventType = stub[EventType]
        val testEventHandler = stub[EventHandler]

        val expected = Seq(
          EventHandlerInfo(TestHandlerId, testEventType, testEventHandler),
          EventHandlerInfo(TestHandlerId + 1, testEventType, testEventHandler),
          EventHandlerInfo(TestHandlerId, testEventType, stub[EventHandler])
        )

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingEventHandlerSupport.processAllPendingEventHandlers()
        actual should be (expected)
      }
    }

    describe("#pendingEventHandlers") {
      it("should return a collection of all pending eventHandlers") {
        val testEventType = stub[EventType]
        val testEventHandler = stub[EventHandler]

        val expected = Seq(
          EventHandlerInfo(TestHandlerId, testEventType, testEventHandler),
          EventHandlerInfo(TestHandlerId + 1, testEventType, testEventHandler),
          EventHandlerInfo(TestHandlerId, testEventType, stub[EventHandler]),
          EventHandlerInfo(TestHandlerId, stub[EventType], testEventHandler)
        )

        val actions = expected.map(ActionInfo.apply("", _: EventHandlerInfo, () => {}))
        (mockPendingActionManager.getPendingActionData _).expects(*).onCall(
          (f: ActionInfo[EventHandlerInfo] => Boolean) =>
            actions.filter(f).map(_.data)
        )

        val actual = pendingEventHandlerSupport.pendingEventHandlers

        actual should be (expected)
      }

      it("should be empty if there are no pending eventHandlers") {
        val expected = Nil

        // No pending eventHandlers
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingEventHandlerSupport.pendingEventHandlers

        actual should be (expected)
      }
    }

    describe("#addEventHandlerWithId") {
      it("should add the event handler to pending if pending enabled") {
        val expected = EventHandlerInfo(
          TestHandlerId,
          stub[EventType],
          stub[EventHandler],
          Seq(stub[JDIEventArgument])
        )

        pendingEventHandlerSupport.enablePendingSupport()

        (mockPendingActionManager.addPendingAction _).expects(expected, *).once()

        val eventHandlerId = pendingEventHandlerSupport.addEventHandlerWithId(
          expected.eventHandlerId,
          expected.eventType,
          expected.eventHandler,
          expected.extraArguments: _*
        )

        eventHandlerId should be (expected.eventHandlerId)
      }

      it("should add the event handler immediately if pending disabled") {
        val expected = EventHandlerInfo(
          TestHandlerId,
          stub[EventType],
          stub[EventHandler],
          Seq(stub[JDIEventArgument])
        )

        // ====================================================================

        // NOTE: Due to limitation of ScalaMock (cannot mock overloaded method),
        //       using DummyEventManager with explicit mock function to verify
        //       specific execution
        val mockAddEventHandlerWithId =
          mockFunction[String, EventType, EventHandler, Seq[JDIEventArgument], String]
        val eventManager = new DummyEventManager {
          override def addEventHandlerWithId(
            eventHandlerId: String,
            eventType: EventType,
            eventHandler: EventHandler,
            eventArguments: JDIEventArgument*
          ): String = mockAddEventHandlerWithId(
            eventHandlerId,
            eventType,
            eventHandler,
            eventArguments
          )
        }

        val pendingEventHandlerSupport = new TestEventManager(
          eventManager
        ) with PendingEventHandlerSupport {
          override protected def newEventId(): String = TestHandlerId

          override protected val pendingActionManager: PendingActionManager[EventHandlerInfo] =
            mockPendingActionManager
        }

        // ====================================================================

        pendingEventHandlerSupport.disablePendingSupport()

        mockAddEventHandlerWithId.expects(
          expected.eventHandlerId,
          expected.eventType,
          expected.eventHandler,
          expected.extraArguments
        ).once()

        val eventHandlerId = pendingEventHandlerSupport.addEventHandlerWithId(
          expected.eventHandlerId,
          expected.eventType,
          expected.eventHandler,
          expected.extraArguments: _*
        )

        eventHandlerId should be (expected.eventHandlerId)
      }
    }
  }
}
