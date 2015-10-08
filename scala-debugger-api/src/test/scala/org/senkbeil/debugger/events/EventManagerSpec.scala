package org.senkbeil.debugger.events

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{Event, EventSet, EventQueue}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

import EventType._
import org.senkbeil.debugger.jdi.events.{JDIEventProcessor, JDIEventArgument}
import org.senkbeil.debugger.jdi.events.filters.JDIEventFilter

class EventManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val mockEventQueue = mock[EventQueue]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class TestEventSetProcessor extends EventSetProcessor(
    eventSet                = stub[EventSet],
    eventFunctionRetrieval  = stubFunction[EventType, Seq[EventManager#EventHandler]],
    onExceptionResume       = true
  )
  private val mockEventSetProcessor = mock[TestEventSetProcessor]

  private class TestEventManager extends EventManager(
    mockVirtualMachine,
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

  describe("EventManager") {
    describe("constructor") {
      it("should start processing events if auto start is enabled") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()

        new EventManager(
          mockVirtualMachine,
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

      it("should start the looping task runner if not already started and flag enabled") {
        val eventManager = new EventManager(
          mockVirtualMachine,
          mockLoopingTaskRunner,
          autoStart = false,
          startTaskRunner = true
        )

        (mockLoopingTaskRunner.isRunning _).expects().returning(false).once()
        (mockLoopingTaskRunner.addTask _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()

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
        createdHandler(mock[Event]) should be (true)
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
        info("First, retrieves the event queue")
        (mockVirtualMachine.eventQueue _).expects()
          .returning(mockEventQueue).once()

        info("Second, removes the next event from the queue")
        (mockEventQueue.remove: Function0[EventSet]).expects()
          .returning(stub[EventSet]).once()

        info("Third, constructs a new event set processor and processes the event")
        (mockEventSetProcessor.process _).expects().once()
      }

      eventManager.eventHandlerTask()
    }
  }

  describe("#newWrapperEventHandler") {
    it("should generate a wrapper that ignores the handler and returns true if the event is denied by a filter") {
      val expected = true

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventProcessor = mock[JDIEventProcessor]
      val mockJdiEventFilter = mock[JDIEventFilter]

      // Filter -> processor, process() == false, never invoke handler
      inSequence {
        (mockJdiEventFilter.toProcessor _).expects()
          .returning(mockJdiEventProcessor).once()
        (mockJdiEventProcessor.process _).expects(*).returning(false).once()
        (mockEventHandler.apply _).expects(*).never()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventFilter)
      )

      val actual = wrapperEventHandler(mock[Event])

      actual should be (expected)
    }

    it("should generate a wrapper that invokes the handler and returns its result if the event is accepted by all filters") {
      val expected = false

      val mockEventHandler = mock[EventManager#EventHandler]
      val mockJdiEventProcessor = mock[JDIEventProcessor]
      val mockJdiEventFilter = mock[JDIEventFilter]

      // Filter -> processor, process() == true,
      // invoke handler and return result
      inSequence {
        (mockJdiEventFilter.toProcessor _).expects()
          .returning(mockJdiEventProcessor).once()
        (mockJdiEventProcessor.process _).expects(*).returning(true).once()
        (mockEventHandler.apply _).expects(*).returning(expected).once()
      }

      val wrapperEventHandler = eventManager.newWrapperEventHandler(
        mockEventHandler,
        Seq(mockJdiEventFilter)
      )

      val actual = wrapperEventHandler(mock[Event])

      actual should be (expected)
    }
  }
}
