package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event.{Event, EventQueue, EventSet}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.data.{JDIEventDataProcessor, JDIEventDataRequest, JDIEventDataResult}
import org.scaladebugger.api.lowlevel.events.filters.{JDIEventFilter, JDIEventFilterProcessor}
import org.scaladebugger.api.lowlevel.events.misc.YesResume
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class StandardEventManagerSpec extends ParallelMockFunSpec
{
  private val TestHandlerId = java.util.UUID.randomUUID().toString
  private val mockEventQueue = mock[EventQueue]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class TestEventSetProcessor extends EventSetProcessor(
    eventSet                = stub[EventSet],
    eventFunctionRetrieval  = stubFunction[EventType, Seq[EventManager#EventHandler]],
    onExceptionResume       = true
  )
  private val mockEventSetProcessor = mock[TestEventSetProcessor]

  private class TestEventManager extends StandardEventManager(
    mockEventQueue,
    mockLoopingTaskRunner,
    autoStart = false
  ) {
    /** Expose the protected task for testing purposes. */
    override def eventHandlerTask(): Unit = super.eventHandlerTask()

    /** Expose the protected task for testing purposes. */
    override  def newWrapperEventHandler(
      eventHandler: EventHandler,
      eventArguments: Seq[JDIEventArgument]
    ): EventHandler = super.newWrapperEventHandler(eventHandler, eventArguments)

    /** Set to a mock to use for verification. */
    override protected def newEventSetProcessor(
      eventSet: EventSet
    ): EventSetProcessor = mockEventSetProcessor
  }
  private val eventManager = new TestEventManager

  describe("StandardEventManager") {
    describe("constructor") {
      it("should start processing events if auto start is enabled") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()

        new StandardEventManager(
          mockEventQueue,
          mockLoopingTaskRunner,
          autoStart = true
        )
      }
    }

    describe("#start") {
      it("should throw an exception if already started") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()
        eventManager.start()

        intercept[AssertionError] {
          eventManager.start()
        }
      }

      it("should add a task to process events") {
        // The event handler task should be added to the looping task runner
        (mockLoopingTaskRunner.addTask _).expects(*).once()

        eventManager.start()
      }
    }

    describe("#isRunning") {
      it("should return true if started") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()

        eventManager.start()

        eventManager.isRunning should be (true)
      }

      it("should return false if not started (or started and then stopped)") {
        eventManager.isRunning should be (false)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        intercept[AssertionError] {
          eventManager.stop()
        }
      }

      it("should remove the task processing events") {
        val taskId = java.util.UUID.randomUUID().toString
        (mockLoopingTaskRunner.addTask _).expects(*).returning(taskId).once()
        eventManager.start()

        // Ensure that removal of task is requested with proper id
        (mockLoopingTaskRunner.removeTask _).expects(taskId).once()
        eventManager.stop()
      }
    }

    describe("#addEventStreamWithId") {
      it("should use the provided id as the event handler id") {
        val expected = java.util.UUID.randomUUID().toString
        val stubEventType = stub[EventType]

        eventManager.addEventStreamWithId(expected, stubEventType)

        val actual = eventManager.getHandlerIdsForEventType(stubEventType).head

        actual should be (expected)
      }
    }

    describe("#addEventStream") {
      it("should send events whenever the underlying event handler is invoked") {
        val stubEventType = stub[EventType]
        val eventDataStream = eventManager.addEventStream(stubEventType)

        val eventHandlerId =
          eventManager.getHandlerIdsForEventType(stubEventType).head
        val eventHandler = eventManager.getEventHandler(eventHandlerId).get

        val mockEvent = mock[Event]

        val mockForeachFunction = mockFunction[Event, Unit]
        eventDataStream.foreach(mockForeachFunction)

        mockForeachFunction.expects(mockEvent).once()

        eventHandler(mockEvent, Nil)
      }

      it("should remove the handler feeding the stream when the stream is closed") {
        val stubEventType = stub[EventType]
        val eventDataStream = eventManager.addEventStream(stubEventType)

        // One event handler added for the above stream
        eventManager.getHandlerIdsForEventType(stubEventType) should
          not be (empty)

        eventDataStream.close(now = true)

        // Event handler removed by the close operation
        eventManager.getHandlerIdsForEventType(stubEventType) should be (empty)
      }
    }

    describe("#addEventDataStreamWithId") {
      it("should use the provided id as the event handler id") {
        val expected = java.util.UUID.randomUUID().toString
        val stubEventType = stub[EventType]

        eventManager.addEventDataStreamWithId(expected, stubEventType)

        val actual = eventManager.getHandlerIdsForEventType(stubEventType).head

        actual should be (expected)
      }
    }

    describe("#addEventDataStream") {
      it("should send events and data whenever the underlying event handler is invoked") {
        val stubEventType = stub[EventType]
        val eventDataStream = eventManager.addEventDataStream(stubEventType)

        val eventHandlerId =
          eventManager.getHandlerIdsForEventType(stubEventType).head
        val eventHandler = eventManager.getEventHandler(eventHandlerId).get

        val mockEvent = mock[Event]

        val mockForeachFunction =
          mockFunction[(Event, Seq[JDIEventDataResult]), Unit]
        eventDataStream.foreach(mockForeachFunction)

        // NOTE: Data is produced by an argument processor, which is annoying
        //       to test in this situation and not relevant to this test
        mockForeachFunction.expects((mockEvent, Nil)).once()

        eventHandler(mockEvent, Nil)
      }

      it("should remove the handler feeding the stream when the stream is closed") {
        val stubEventType = stub[EventType]
        val eventDataStream = eventManager.addEventDataStream(stubEventType)

        // One event handler added for the above stream
        eventManager.getHandlerIdsForEventType(stubEventType) should
          not be (empty)

        eventDataStream.close(now = true)

        // Event handler removed by the close operation
        eventManager.getHandlerIdsForEventType(stubEventType) should be (empty)
      }
    }

    describe("#addResumingEventHandlerWithId") {
      it("should use the provided id as the event handler id") {
        val expected = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )
        val stubEventType = stub[EventType]

        eventManager.addResumingEventHandlerWithId(
          expected.head,
          stubEventType,
          (_: Event) => {}
        )

        eventManager.addResumingEventHandlerWithId(
          expected.last,
          stubEventType,
          (_: Event, _: Seq[JDIEventDataResult]) => {}
        )

        val actual = eventManager.getHandlerIdsForEventType(stubEventType)

        actual should contain theSameElementsAs (expected)
      }
    }

    describe("#addResumingEventHandler") {
      it("should compose the provided unit function to return true") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]

        // Add a unit function as an event handler
        val eventHandlerId =
          eventManager.addResumingEventHandler(stubEventType, _ => {})

        // Retrieve the added handler
        val createdHandler = eventManager.getEventHandler(eventHandlerId).get

        // Verify that the handler returns true when invoked
        createdHandler(mock[Event], Nil) should be (true)
      }
    }

    describe("#addEventHandlerWithId") {
      it("should use the provided id as the event handler id") {
        val expected = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )
        val stubEventType = stub[EventType]

        eventManager.addEventHandlerWithId(
          expected.head,
          stubEventType,
          (_: Event) => false
        )

        eventManager.addEventHandlerWithId(
          expected.last,
          stubEventType,
          (_: Event, _: Seq[JDIEventDataResult]) => false
        )

        val actual = eventManager.getHandlerIdsForEventType(stubEventType)

        actual should contain theSameElementsAs (expected)
      }
    }

    describe("#addEventHandler") {
      it("should add to the existing collection of handlers") {
        val totalHandlers = 3

        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]

        val eventHandlers = (1 to totalHandlers)
          .map(_ => mock[EventManager#EventHandler])

        eventHandlers.foreach { eventHandler =>
          eventManager.addEventHandler(stubEventType, eventHandler)
        }

        eventManager.getHandlersForEventType(stubEventType).length should be (3)
      }

      it("should become the first handler if no others exist for the event type") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val eventHandler = mock[EventManager#EventHandler]

        val eventHandlerId =
          eventManager.addEventHandler(stubEventType, eventHandler)

        eventManager.getHandlersForEventType(stubEventType).length should be (1)
      }
    }

    describe("#getHandlersForEventType") {
      it("should return an empty collection if no handlers are found") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]

        eventManager.getHandlersForEventType(stubEventType) should be (empty)
      }

      it("should return a collection of handlers for the event type") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val mockEventHandler = mock[EventManager#EventHandler]

        eventManager.addEventHandler(stubEventType, mockEventHandler)

        eventManager.getHandlersForEventType(stubEventType) should not be (empty)
      }
    }

    describe("#getHandlerIdsForEventType") {
      it("should return an empty collection if no handlers are found") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]

        eventManager.getHandlerIdsForEventType(stubEventType) should be (empty)
      }

      it("should return a collection of handler ids for the event type") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val mockEventHandler = mock[EventManager#EventHandler]

        eventManager.addEventHandler(stubEventType, mockEventHandler)

        eventManager.getHandlerIdsForEventType(stubEventType) should not be (empty)
      }
    }

    describe("#getEventHandler") {
      it("should return None if the handler is not found") {
        val expected = None

        val actual = eventManager.getEventHandler("some id")

        actual should be (expected)
      }

      it("should return Some(EventHandler) if the handler is found") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val mockEventHandler = mock[EventManager#EventHandler]

        val eventHandlerId =
          eventManager.addEventHandler(stubEventType, mockEventHandler)

        // NOTE: The handler returned is the wrapped one, so we cannot compare
        //       it against the original handler without messing with the
        //       wrapping function.
        eventManager.getEventHandler(eventHandlerId) should not be (None)
      }
    }

    describe("#getAllEventHandlerInfo") {
      it("should return an empty collection if no handlers are found") {
        val expected = Nil

        val actual = eventManager.getAllEventHandlerInfo

        actual should be (expected)
      }

      it("should return a collection of handler info for all available handlers") {
        val expected = EventHandlerInfo(
          TestHandlerId,
          stub[EventType],
          stub[EventManager#EventHandler]
        )

        eventManager.addEventHandlerFromInfo(expected)

        val eventHandlerInfoList = eventManager.getAllEventHandlerInfo
        val actual = eventHandlerInfoList.head

        // NOTE: The event handler is wrapped, so we cannot use a simple
        //       direct comparison
        eventHandlerInfoList should have length (1)
        actual.eventHandlerId should be (expected.eventHandlerId)
        actual.eventType should be (expected.eventType)
        actual.extraArguments should be (expected.extraArguments)
      }
    }

    describe("#removeEventHandler") {
      it("should return None if the handler is not found") {
        val expected = None

        val actual = eventManager.removeEventHandler("some id")

        actual should be (expected)
      }

      it("should remove the event handler if found") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val mockEventHandler = mock[EventManager#EventHandler]

        val eventHandlerId =
          eventManager.addEventHandler(stubEventType, mockEventHandler)

        eventManager.getHandlersForEventType(stubEventType) should not be (empty)

        eventManager.removeEventHandler(eventHandlerId)

        eventManager.getHandlersForEventType(stubEventType) should be (empty)
      }

      it("should return Some(EventHandler) if found") {
        // Using a stub to avoid hacks for EventType.toString()
        val stubEventType = stub[EventType]
        val mockEventHandler = mock[EventManager#EventHandler]

        val eventHandlerId =
          eventManager.addEventHandler(stubEventType, mockEventHandler)

        // NOTE: The handler returned is the wrapped one, so we cannot compare
        //       it against the original handler without messing with the
        //       wrapping function.
        eventManager.removeEventHandler(eventHandlerId) should not be (None)
      }
    }
  }

  describe("#eventHandlerTask") {
    it("should execute steps to process event handlers for an event") {
      inSequence {
        info("First, removes the next event from the queue")
        (mockEventQueue.remove: Function0[EventSet]).expects()
          .returning(stub[EventSet]).once()

        info("Second, constructs a new event set processor and processes the event")
        (mockEventSetProcessor.process _).expects().once()
      }

      eventManager.eventHandlerTask()
    }
  }

  describe("#newWrapperEventHandler") {
    it("should generate a wrapper that ignores the handler and returns true if the event is denied by a filter") {
      val expected = true

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventFilterProcessor = mock[JDIEventFilterProcessor]
      val mockJdiEventFilter = mock[JDIEventFilter]

      // Filter -> processor, process() == false, never invoke handler
      inSequence {
        (mockJdiEventFilter.toProcessor _).expects()
          .returning(mockJdiEventFilterProcessor).once()
        (mockJdiEventFilterProcessor.process _).expects(*)
          .returning(false).once()
        (mockEventHandler.apply _).expects(*, *).never()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventFilter)
      )

      val actual = wrapperEventHandler(mock[Event], Nil)

      actual should be (expected)
    }

    it("should generate a wrapper that invokes the handler and returns its result if the event is accepted by all filters") {
      val expected = false

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventFilterProcessor = mock[JDIEventFilterProcessor]
      val mockJdiEventFilter = mock[JDIEventFilter]

      // Filter -> processor, process() == true,
      // invoke handler and return result
      inSequence {
        (mockJdiEventFilter.toProcessor _).expects()
          .returning(mockJdiEventFilterProcessor).once()
        (mockJdiEventFilterProcessor.process _).expects(*)
          .returning(true).once()
        (mockEventHandler.apply _).expects(*, *).returning(expected).once()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventFilter)
      )

      val actual = wrapperEventHandler(mock[Event], Nil)

      actual should be (expected)
    }

    it("should generate a wrapper that invokes the handler and uses the resume value if provided") {
      val expected = true

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventFilterProcessor = mock[JDIEventFilterProcessor]
      val mockJdiEventFilter = mock[JDIEventFilter]

      // Filter -> processor, process() == true,
      // invoke handler and return result of false
      inSequence {
        (mockJdiEventFilter.toProcessor _).expects()
          .returning(mockJdiEventFilterProcessor).once()
        (mockJdiEventFilterProcessor.process _).expects(*)
          .returning(true).once()
        (mockEventHandler.apply _).expects(*, *).returning(false).once()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventFilter, YesResume)
      )

      // Result of invocation would normally be false, but overriden
      // with YesResume
      val actual = wrapperEventHandler(mock[Event], Nil)

      actual should be (expected)
    }

    it("should pass any data from the invoked processor to the event handler") {
      val expected = mock[JDIEventDataResult]

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventDataRequest = mock[JDIEventDataRequest]
      val mockJdiEventDataProcessor = mock[JDIEventDataProcessor]

      // Request -> processor, request.process() == data,
      // invoke handler and return result
      inSequence {
        (mockJdiEventDataRequest.toProcessor _).expects()
          .returning(mockJdiEventDataProcessor).once()
        (mockJdiEventDataProcessor.process _).expects(*)
          .returning(Seq(expected)).once()
        (mockEventHandler.apply _).expects(*, Seq(expected)).once()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventDataRequest)
      )

      wrapperEventHandler(mock[Event], Nil)
    }
  }
}
