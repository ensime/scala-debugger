package org.scaladebugger.api.lowlevel.events

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}
import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scaladebugger.api.lowlevel.events.EventType.EventType

class DummyEventManagerSpec extends test.ParallelMockFunSpec
{
  private val TestHandlerId = java.util.UUID.randomUUID().toString
  private val eventManager = new DummyEventManager

  describe("DummyEventManager") {
    describe("#addEventHandlerWithId") {
      it("should return the provided id") {
        val expected = TestHandlerId

        val actual = eventManager.addEventHandlerWithId(
          expected,
          mock[EventType],
          mock[EventHandler]
        )

        actual should be (expected)
      }
    }

    describe("#getHandlersForEventType") {
      it("should return an empty list") {
        val expected = Nil

        val actual = eventManager.getHandlersForEventType(mock[EventType])

        actual should be (expected)
      }
    }

    describe("#getHandlerIdsForEventType") {
      it("should return an empty list") {
        val expected = Nil

        val actual = eventManager.getHandlerIdsForEventType(mock[EventType])

        actual should be (expected)
      }
    }

    describe("#getEventHandler") {
      it("should return None") {
        val expected = None

        val actual = eventManager.getEventHandler(TestHandlerId)

        actual should be (expected)
      }
    }

    describe("#getAllEventHandlerInfo") {
      it("should return an empty list") {
        val expected = Nil

        val actual = eventManager.getAllEventHandlerInfo

        actual should be (expected)
      }
    }

    describe("#removeEventHandler") {
      it("should return None") {
        val expected = None

        val actual = eventManager.removeEventHandler(TestHandlerId)

        actual should be (expected)
      }
    }

    describe("#start") {
      it("should do nothing") {
        eventManager.start()
      }
    }

    describe("#stop") {
      it("should do nothing") {
        eventManager.stop()
      }
    }
  }
}
