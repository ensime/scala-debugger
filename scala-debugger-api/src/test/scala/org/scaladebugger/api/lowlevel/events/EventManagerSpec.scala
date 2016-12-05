package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.{JDIEventDataResult, JDIEventDataRequest}
import test.TestEventManager

class EventManagerSpec extends test.ParallelMockFunSpec
{
  private val TestHandlerId = java.util.UUID.randomUUID().toString
  private val mockEventManager = mock[EventManager]
  private val mockAddEventHandlerWithId =
    mockFunction[String, EventType, EventHandler, Seq[JDIEventArgument], String]
  private val testEventManager = new TestEventManager(
    mockEventManager
  ) {
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

    override protected def newEventId(): String = TestHandlerId
  }

  describe("EventManager") {
    describe("#addEventStream") {
      it("should invoke addEventHandlerWithId") {
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(TestHandlerId).once()

        testEventManager.addEventStream(
          testEventType,
          testExtraArguments: _*
        )
      }

      it("should include the event handler id in the pipeline metadata") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId.expects(*, *, *, *).returning(expected).once()

        val pipeline = testEventManager.addEventStream(
          testEventType,
          testExtraArguments: _*
        )

        val actual = pipeline.currentMetadata(
          EventManager.EventHandlerIdMetadataField
        )

        actual should be (expected)
      }
    }

    describe("#addEventStreamWithId") {
      it("should invoke addEventHandlerWithId") {
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(TestHandlerId).once()

        testEventManager.addEventStreamWithId(
          TestHandlerId,
          testEventType,
          testExtraArguments: _*
        )
      }

      it("should include the event handler id in the pipeline metadata") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId.expects(*, *, *, *).returning(expected).once()

        val pipeline = testEventManager.addEventStreamWithId(
          expected,
          testEventType,
          testExtraArguments: _*
        )

        val actual = pipeline.currentMetadata(
          EventManager.EventHandlerIdMetadataField
        )

        actual should be (expected)
      }
    }

    describe("#addEventDataStream") {
      it("should invoke addEventHandlerWithId") {
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(TestHandlerId).once()

        testEventManager.addEventDataStream(
          testEventType,
          testExtraArguments: _*
        )
      }

      it("should include the event handler id in the pipeline metadata") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId.expects(*, *, *, *).returning(expected).once()

        val pipeline = testEventManager.addEventDataStream(
          testEventType,
          testExtraArguments: _*
        )

        val actual = pipeline.currentMetadata(
          EventManager.EventHandlerIdMetadataField
        )

        actual should be (expected)
      }
    }

    describe("#addEventDataStreamWithId") {
      it("should invoke addEventHandlerWithId") {
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(TestHandlerId).once()

        testEventManager.addEventDataStreamWithId(
          TestHandlerId,
          testEventType,
          testExtraArguments: _*
        )
      }

      it("should include the event handler id in the pipeline metadata") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId.expects(*, *, *, *).returning(expected).once()

        val pipeline = testEventManager.addEventDataStreamWithId(
          expected,
          testEventType,
          testExtraArguments: _*
        )

        val actual = pipeline.currentMetadata(
          EventManager.EventHandlerIdMetadataField
        )

        actual should be (expected)
      }
    }

    describe("#addResumingEventHandler with EventAndData => Unit") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mock[(Event, Seq[JDIEventDataResult]) => Unit]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addResumingEventHandler(
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addResumingEventHandler with Event => Unit") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mockFunction[Event, Unit]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addResumingEventHandler(
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addResumingEventHandlerWithId with EventAndData => Unit") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mock[(Event, Seq[JDIEventDataResult]) => Unit]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addResumingEventHandlerWithId(
          expected,
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addResumingEventHandlerWithId with Event => Unit") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mockFunction[Event, Unit]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addResumingEventHandlerWithId(
          expected,
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addEventHandler with EventAndData => Boolean") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = stub[EventHandler]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, testEventHandler, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addEventHandler(
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addEventHandler with Event => Boolean") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mockFunction[Event, Boolean]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addEventHandler(
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addEventHandlerWithId with Event => Boolean") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = mockFunction[Event, Boolean]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, *, testExtraArguments)
          .returning(expected).once()

        val actual = testEventManager.addEventHandlerWithId(
          expected,
          testEventType,
          testEventHandler,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#addEventHandlerFromInfo") {
      it("should invoke addEventHandlerWithId") {
        val expected = TestHandlerId
        val testEventType = stub[EventType]
        val testEventHandler = stub[EventHandler]
        val testExtraArguments = Seq(stub[JDIEventArgument])

        mockAddEventHandlerWithId
          .expects(TestHandlerId, testEventType, testEventHandler, testExtraArguments)
          .returning(expected).once()

        val info = EventHandlerInfo(
          TestHandlerId,
          testEventType,
          testEventHandler,
          testExtraArguments
        )
        val actual = testEventManager.addEventHandlerFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
