package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.reflect.ClassTag

class PureEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockEvent = mock[Event]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  // Type is used to fake our event class type
  private class TestPureEventInfo[T](
    private val event: Event = mockEvent
  )(
    implicit val classTag: ClassTag[T]
  ) extends PureEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    event = event,
    jdiArguments = mockJdiArguments
  ) {
    override protected def eventClass: Class[_] = classTag.runtimeClass
  }

  private val pureEventInfoProfile = new TestPureEventInfo[Event]()

  describe("PureEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[EventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        (mockEventInfoProducer.newEventInfo _)
          .expects(mockScalaVirtualMachine, mockEvent, mockJdiArguments)
          .returning(expected).once()

        val actual = pureEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockEvent

        val actual = pureEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#requestArguments") {
      it("should return all request arguments from the provided JDI arguments") {
        val expected = mockJdiRequestArguments

        val actual = pureEventInfoProfile.requestArguments

        actual should contain theSameElementsAs expected
      }
    }

    describe("#eventArguments") {
      it("should return all request arguments from the provided JDI arguments") {
        val expected = mockJdiEventArguments

        val actual = pureEventInfoProfile.eventArguments

        actual should contain theSameElementsAs expected
      }
    }

    describe("#toString") {
      it("should return the string representation of the JDI event object") {
        val expected = mockEvent.toString // NOTE: Cannot mock toString method

        val actual = pureEventInfoProfile.toString

        actual should be (expected)
      }
    }

    describe("#isPlainEvent") {
      it("should return true if the JDI event has a class of Event") {
        val expected = true

        val pureEventInfoProfile = new TestPureEventInfo[Event]()
        val actual = pureEventInfoProfile.isPlainEvent

        actual should be (expected)
      }

      it("should return false if the JDI event has a class of anything higher than Event") {
        val expected = false

        // LocatableEvent is not exactly an Event
        val pureEventInfoProfile = new TestPureEventInfo[LocatableEvent]()
        val actual = pureEventInfoProfile.isPlainEvent

        actual should be (expected)
      }
    }

    describe("#isAccessWatchpointEvent") {
      it("should return true if the event inherits from AccessWatchpointEvent") {
        val expected = true

        val mockAccessWatchpointEvent = mock[AccessWatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[AccessWatchpointEvent](
          event = mockAccessWatchpointEvent
        )
        val actual = pureEventInfoProfile.isAccessWatchpointEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from AccessWatchpointEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isAccessWatchpointEvent

        actual should be (expected)
      }
    }

    describe("#isBreakpointEvent") {
      it("should return true if the event inherits from BreakpointEvent") {
        val expected = true

        val mockBreakpointEvent = mock[BreakpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[BreakpointEvent](
          event = mockBreakpointEvent
        )
        val actual = pureEventInfoProfile.isBreakpointEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from BreakpointEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isBreakpointEvent

        actual should be (expected)
      }
    }

    describe("#isClassPrepareEvent") {
      it("should return true if the event inherits from ClassPrepareEvent") {
        val expected = true

        val mockClassPrepareEvent = mock[ClassPrepareEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ClassPrepareEvent](
          event = mockClassPrepareEvent
        )
        val actual = pureEventInfoProfile.isClassPrepareEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ClassPrepareEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isClassPrepareEvent

        actual should be (expected)
      }
    }

    describe("#isClassUnloadEvent") {
      it("should return true if the event inherits from ClassUnloadEvent") {
        val expected = true

        val mockClassUnloadEvent = mock[ClassUnloadEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ClassUnloadEvent](
          event = mockClassUnloadEvent
        )
        val actual = pureEventInfoProfile.isClassUnloadEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ClassUnloadEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isClassUnloadEvent

        actual should be (expected)
      }
    }

    describe("#isExceptionEvent") {
      it("should return true if the event inherits from ExceptionEvent") {
        val expected = true

        val mockExceptionEvent = mock[ExceptionEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ExceptionEvent](
          event = mockExceptionEvent
        )
        val actual = pureEventInfoProfile.isExceptionEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ExceptionEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isExceptionEvent

        actual should be (expected)
      }
    }

    describe("#isLocatableEvent") {
      it("should return true if the event inherits from LocatableEvent") {
        val expected = true

        val mockLocatableEvent = mock[LocatableEvent]
        val pureEventInfoProfile = new TestPureEventInfo[LocatableEvent](
          event = mockLocatableEvent
        )
        val actual = pureEventInfoProfile.isLocatableEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from LocatableEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isLocatableEvent

        actual should be (expected)
      }
    }

    describe("#isMethodEntryEvent") {
      it("should return true if the event inherits from MethodEntryEvent") {
        val expected = true

        val mockMethodEntryEvent = mock[MethodEntryEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MethodEntryEvent](
          event = mockMethodEntryEvent
        )
        val actual = pureEventInfoProfile.isMethodEntryEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MethodEntryEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMethodEntryEvent

        actual should be (expected)
      }
    }

    describe("#isMethodExitEvent") {
      it("should return true if the event inherits from MethodExitEvent") {
        val expected = true

        val mockMethodExitEvent = mock[MethodExitEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MethodExitEvent](
          event = mockMethodExitEvent
        )
        val actual = pureEventInfoProfile.isMethodExitEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MethodExitEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMethodExitEvent

        actual should be (expected)
      }
    }

    describe("#isModificationWatchpointEvent") {
      it("should return true if the event inherits from ModificationWatchpointEvent") {
        val expected = true

        val mockModificationWatchpointEvent = mock[ModificationWatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ModificationWatchpointEvent](
          event = mockModificationWatchpointEvent
        )
        val actual = pureEventInfoProfile.isModificationWatchpointEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ModificationWatchpointEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isModificationWatchpointEvent

        actual should be (expected)
      }
    }

    describe("#isMonitorContendedEnteredEvent") {
      it("should return true if the event inherits from MonitorContendedEnteredEvent") {
        val expected = true

        val mockMonitorContendedEnteredEvent = mock[MonitorContendedEnteredEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorContendedEnteredEvent](
          event = mockMonitorContendedEnteredEvent
        )
        val actual = pureEventInfoProfile.isMonitorContendedEnteredEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MonitorContendedEnteredEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMonitorContendedEnteredEvent

        actual should be (expected)
      }
    }

    describe("#isMonitorContendedEnterEvent") {
      it("should return true if the event inherits from MonitorContendedEnterEvent") {
        val expected = true

        val mockMonitorContendedEnterEvent = mock[MonitorContendedEnterEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorContendedEnterEvent](
          event = mockMonitorContendedEnterEvent
        )
        val actual = pureEventInfoProfile.isMonitorContendedEnterEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MonitorContendedEnterEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMonitorContendedEnterEvent

        actual should be (expected)
      }
    }

    describe("#isMonitorWaitedEvent") {
      it("should return true if the event inherits from MonitorWaitedEvent") {
        val expected = true

        val mockMonitorWaitedEvent = mock[MonitorWaitedEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorWaitedEvent](
          event = mockMonitorWaitedEvent
        )
        val actual = pureEventInfoProfile.isMonitorWaitedEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MonitorWaitedEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMonitorWaitedEvent

        actual should be (expected)
      }
    }

    describe("#isMonitorWaitEvent") {
      it("should return true if the event inherits from MonitorWaitEvent") {
        val expected = true

        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorWaitEvent](
          event = mockMonitorWaitEvent
        )
        val actual = pureEventInfoProfile.isMonitorWaitEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from MonitorWaitEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isMonitorWaitEvent

        actual should be (expected)
      }
    }

    describe("#isStepEvent") {
      it("should return true if the event inherits from StepEvent") {
        val expected = true

        val mockStepEvent = mock[StepEvent]
        val pureEventInfoProfile = new TestPureEventInfo[StepEvent](
          event = mockStepEvent
        )
        val actual = pureEventInfoProfile.isStepEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from StepEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isStepEvent

        actual should be (expected)
      }
    }

    describe("#isThreadDeathEvent") {
      it("should return true if the event inherits from ThreadDeathEvent") {
        val expected = true

        val mockThreadDeathEvent = mock[ThreadDeathEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ThreadDeathEvent](
          event = mockThreadDeathEvent
        )
        val actual = pureEventInfoProfile.isThreadDeathEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ThreadDeathEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isThreadDeathEvent

        actual should be (expected)
      }
    }

    describe("#isThreadStartEvent") {
      it("should return true if the event inherits from ThreadStartEvent") {
        val expected = true

        val mockThreadStartEvent = mock[ThreadStartEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ThreadStartEvent](
          event = mockThreadStartEvent
        )
        val actual = pureEventInfoProfile.isThreadStartEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from ThreadStartEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isThreadStartEvent

        actual should be (expected)
      }
    }

    describe("#isVMDeathEvent") {
      it("should return true if the event inherits from VMDeathEvent") {
        val expected = true

        val mockVMDeathEvent = mock[VMDeathEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMDeathEvent](
          event = mockVMDeathEvent
        )
        val actual = pureEventInfoProfile.isVMDeathEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from VMDeathEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isVMDeathEvent

        actual should be (expected)
      }
    }

    describe("#isVMDisconnectEvent") {
      it("should return true if the event inherits from VMDisconnectEvent") {
        val expected = true

        val mockVMDisconnectEvent = mock[VMDisconnectEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMDisconnectEvent](
          event = mockVMDisconnectEvent
        )
        val actual = pureEventInfoProfile.isVMDisconnectEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from VMDisconnectEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isVMDisconnectEvent

        actual should be (expected)
      }
    }

    describe("#isVMStartEvent") {
      it("should return true if the event inherits from VMStartEvent") {
        val expected = true

        val mockVMStartEvent = mock[VMStartEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMStartEvent](
          event = mockVMStartEvent
        )
        val actual = pureEventInfoProfile.isVMStartEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from VMStartEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isVMStartEvent

        actual should be (expected)
      }
    }

    describe("#isWatchpointEvent") {
      it("should return true if the event inherits from WatchpointEvent") {
        val expected = true

        val mockWatchpointEvent = mock[WatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[WatchpointEvent](
          event = mockWatchpointEvent
        )
        val actual = pureEventInfoProfile.isWatchpointEvent

        actual should be (expected)
      }

      it("should return false if the event does not inherit from WatchpointEvent") {
        val expected = false

        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )
        val actual = pureEventInfoProfile.isWatchpointEvent

        actual should be (expected)
      }
    }

    describe("#toAccessWatchpointEvent") {
      it("should throw an assertion error if the event does not inherit from AccessWatchpointEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toAccessWatchpointEvent
      }

      it("should return a new copy of AccessWatchpointEventProfile using the event producer") {
        val expected = mock[AccessWatchpointEventInfo]

        val mockAccessWatchpointEvent = mock[AccessWatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[AccessWatchpointEvent](
          event = mockAccessWatchpointEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultAccessWatchpointEventInfo _).expects(
          mockScalaVirtualMachine,
          mockAccessWatchpointEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toAccessWatchpointEvent

        actual should be (expected)
      }
    }

    describe("#toBreakpointEvent") {
      it("should throw an assertion error if the event does not inherit from BreakpointEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toBreakpointEvent
      }

      it("should return a new copy of BreakpointEventProfile using the event producer") {
        val expected = mock[BreakpointEventInfo]

        val mockBreakpointEvent = mock[BreakpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[BreakpointEvent](
          event = mockBreakpointEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultBreakpointEventInfo _).expects(
          mockScalaVirtualMachine,
          mockBreakpointEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toBreakpointEvent

        actual should be (expected)
      }
    }

    describe("#toClassPrepareEvent") {
      it("should throw an assertion error if the event does not inherit from ClassPrepareEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toClassPrepareEvent
      }

      it("should return a new copy of ClassPrepareEventProfile using the event producer") {
        val expected = mock[ClassPrepareEventInfo]

        val mockClassPrepareEvent = mock[ClassPrepareEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ClassPrepareEvent](
          event = mockClassPrepareEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultClassPrepareEventInfo _).expects(
          mockScalaVirtualMachine,
          mockClassPrepareEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toClassPrepareEvent

        actual should be (expected)
      }
    }

    describe("#toClassUnloadEvent") {
      it("should throw an assertion error if the event does not inherit from ClassUnloadEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toClassUnloadEvent
      }

      it("should return a new copy of ClassUnloadEventProfile using the event producer") {
        val expected = mock[ClassUnloadEventInfo]

        val mockClassUnloadEvent = mock[ClassUnloadEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ClassUnloadEvent](
          event = mockClassUnloadEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultClassUnloadEventInfo _).expects(
          mockScalaVirtualMachine,
          mockClassUnloadEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toClassUnloadEvent

        actual should be (expected)
      }
    }

    describe("#toExceptionEvent") {
      it("should throw an assertion error if the event does not inherit from ExceptionEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toExceptionEvent
      }

      it("should return a new copy of ExceptionEventProfile using the event producer") {
        val expected = mock[ExceptionEventInfo]

        val mockExceptionEvent = mock[ExceptionEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ExceptionEvent](
          event = mockExceptionEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultExceptionEventInfo _).expects(
          mockScalaVirtualMachine,
          mockExceptionEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toExceptionEvent

        actual should be (expected)
      }
    }

    describe("#toLocatableEvent") {
      it("should throw an assertion error if the event does not inherit from LocatableEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toLocatableEvent
      }

      it("should return a new copy of LocatableEventProfile using the event producer") {
        val expected = mock[LocatableEventInfo]

        val mockLocatableEvent = mock[LocatableEvent]
        val pureEventInfoProfile = new TestPureEventInfo[LocatableEvent](
          event = mockLocatableEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultLocatableEventInfo _).expects(
          mockScalaVirtualMachine,
          mockLocatableEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toLocatableEvent

        actual should be (expected)
      }
    }

    describe("#toMethodEntryEvent") {
      it("should throw an assertion error if the event does not inherit from MethodEntryEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMethodEntryEvent
      }

      it("should return a new copy of MethodEntryEventProfile using the event producer") {
        val expected = mock[MethodEntryEventInfo]

        val mockMethodEntryEvent = mock[MethodEntryEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MethodEntryEvent](
          event = mockMethodEntryEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMethodEntryEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMethodEntryEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMethodEntryEvent

        actual should be (expected)
      }
    }

    describe("#toMethodExitEvent") {
      it("should throw an assertion error if the event does not inherit from MethodExitEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMethodExitEvent
      }

      it("should return a new copy of MethodExitEventProfile using the event producer") {
        val expected = mock[MethodExitEventInfo]

        val mockMethodExitEvent = mock[MethodExitEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MethodExitEvent](
          event = mockMethodExitEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMethodExitEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMethodExitEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMethodExitEvent

        actual should be (expected)
      }
    }

    describe("#toModificationWatchpointEvent") {
      it("should throw an assertion error if the event does not inherit from ModificationWatchpointEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toModificationWatchpointEvent
      }

      it("should return a new copy of ModificationWatchpointEventProfile using the event producer") {
        val expected = mock[ModificationWatchpointEventInfo]

        val mockModificationWatchpointEvent = mock[ModificationWatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ModificationWatchpointEvent](
          event = mockModificationWatchpointEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultModificationWatchpointEventInfo _).expects(
          mockScalaVirtualMachine,
          mockModificationWatchpointEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toModificationWatchpointEvent

        actual should be (expected)
      }
    }

    describe("#toMonitorEvent") {
      it("should throw an assertion error if the event does not inherit from a monitor event") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMonitorEvent
      }

      it("should return a new copy of MonitorEventProfile using the event producer") {
        val expected = mock[MonitorEventInfo]

        // Set up our event (a real monitor event) to be wrapped in our pseudo
        // monitor event class
        val mockMonitorEvent = mock[MonitorWaitEvent]
        val monitorEvent = new MonitorEvent(mockMonitorEvent)
        val pureEventInfoProfile = new TestPureEventInfo[MonitorEvent](
          event = mockMonitorEvent
        ) {
          override protected def newMonitorEvent(
            locatableEvent: LocatableEvent
          ): MonitorEvent = monitorEvent
        }

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMonitorEventInfo _).expects(
          mockScalaVirtualMachine,
          monitorEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMonitorEvent

        actual should be (expected)
      }
    }

    describe("#toMonitorContendedEnteredEvent") {
      it("should throw an assertion error if the event does not inherit from MonitorContendedEnteredEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMonitorContendedEnteredEvent
      }

      it("should return a new copy of MonitorContendedEnteredEventProfile using the event producer") {
        val expected = mock[MonitorContendedEnteredEventInfo]

        val mockMonitorContendedEnteredEvent = mock[MonitorContendedEnteredEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorContendedEnteredEvent](
          event = mockMonitorContendedEnteredEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMonitorContendedEnteredEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMonitorContendedEnteredEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMonitorContendedEnteredEvent

        actual should be (expected)
      }
    }

    describe("#toMonitorContendedEnterEvent") {
      it("should throw an assertion error if the event does not inherit from MonitorContendedEnterEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMonitorContendedEnterEvent
      }

      it("should return a new copy of MonitorContendedEnterEventProfile using the event producer") {
        val expected = mock[MonitorContendedEnterEventInfo]

        val mockMonitorContendedEnterEvent = mock[MonitorContendedEnterEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorContendedEnterEvent](
          event = mockMonitorContendedEnterEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMonitorContendedEnterEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMonitorContendedEnterEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMonitorContendedEnterEvent

        actual should be (expected)
      }
    }

    describe("#toMonitorWaitedEvent") {
      it("should throw an assertion error if the event does not inherit from MonitorWaitedEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMonitorWaitedEvent
      }

      it("should return a new copy of MonitorWaitedEventProfile using the event producer") {
        val expected = mock[MonitorWaitedEventInfo]

        val mockMonitorWaitedEvent = mock[MonitorWaitedEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorWaitedEvent](
          event = mockMonitorWaitedEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMonitorWaitedEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMonitorWaitedEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMonitorWaitedEvent

        actual should be (expected)
      }
    }

    describe("#toMonitorWaitEvent") {
      it("should throw an assertion error if the event does not inherit from MonitorWaitEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toMonitorWaitEvent
      }

      it("should return a new copy of MonitorWaitEventProfile using the event producer") {
        val expected = mock[MonitorWaitEventInfo]

        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val pureEventInfoProfile = new TestPureEventInfo[MonitorWaitEvent](
          event = mockMonitorWaitEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultMonitorWaitEventInfo _).expects(
          mockScalaVirtualMachine,
          mockMonitorWaitEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toMonitorWaitEvent

        actual should be (expected)
      }
    }

    describe("#toStepEvent") {
      it("should throw an assertion error if the event does not inherit from StepEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toStepEvent
      }

      it("should return a new copy of StepEventProfile using the event producer") {
        val expected = mock[StepEventInfo]

        val mockStepEvent = mock[StepEvent]
        val pureEventInfoProfile = new TestPureEventInfo[StepEvent](
          event = mockStepEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toStepEvent

        actual should be (expected)
      }
    }

    describe("#toThreadDeathEvent") {
      it("should throw an assertion error if the event does not inherit from ThreadDeathEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toThreadDeathEvent
      }

      it("should return a new copy of ThreadDeathEventProfile using the event producer") {
        val expected = mock[ThreadDeathEventInfo]

        val mockThreadDeathEvent = mock[ThreadDeathEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ThreadDeathEvent](
          event = mockThreadDeathEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultThreadDeathEventInfo _).expects(
          mockScalaVirtualMachine,
          mockThreadDeathEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toThreadDeathEvent

        actual should be (expected)
      }
    }

    describe("#toThreadStartEvent") {
      it("should throw an assertion error if the event does not inherit from ThreadStartEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toThreadStartEvent
      }

      it("should return a new copy of ThreadStartEventProfile using the event producer") {
        val expected = mock[ThreadStartEventInfo]

        val mockThreadStartEvent = mock[ThreadStartEvent]
        val pureEventInfoProfile = new TestPureEventInfo[ThreadStartEvent](
          event = mockThreadStartEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultThreadStartEventInfo _).expects(
          mockScalaVirtualMachine,
          mockThreadStartEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toThreadStartEvent

        actual should be (expected)
      }
    }

    describe("#toVMDeathEvent") {
      it("should throw an assertion error if the event does not inherit from VMDeathEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toVMDeathEvent
      }

      it("should return a new copy of VMDeathEventProfile using the event producer") {
        val expected = mock[VMDeathEventInfo]

        val mockVMDeathEvent = mock[VMDeathEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMDeathEvent](
          event = mockVMDeathEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultVMDeathEventInfo _).expects(
          mockScalaVirtualMachine,
          mockVMDeathEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toVMDeathEvent

        actual should be (expected)
      }
    }

    describe("#toVMDisconnectEvent") {
      it("should throw an assertion error if the event does not inherit from VMDisconnectEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toVMDisconnectEvent
      }

      it("should return a new copy of VMDisconnectEventProfile using the event producer") {
        val expected = mock[VMDisconnectEventInfo]

        val mockVMDisconnectEvent = mock[VMDisconnectEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMDisconnectEvent](
          event = mockVMDisconnectEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultVMDisconnectEventInfo _).expects(
          mockScalaVirtualMachine,
          mockVMDisconnectEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toVMDisconnectEvent

        actual should be (expected)
      }
    }

    describe("#toVMStartEvent") {
      it("should throw an assertion error if the event does not inherit from VMStartEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toVMStartEvent
      }

      it("should return a new copy of VMStartEventProfile using the event producer") {
        val expected = mock[VMStartEventInfo]

        val mockVMStartEvent = mock[VMStartEvent]
        val pureEventInfoProfile = new TestPureEventInfo[VMStartEvent](
          event = mockVMStartEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultVMStartEventInfo _).expects(
          mockScalaVirtualMachine,
          mockVMStartEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toVMStartEvent

        actual should be (expected)
      }
    }

    describe("#toWatchpointEvent") {
      it("should throw an assertion error if the event does not inherit from WatchpointEvent") {
        val mockEvent = mock[Event]
        val pureEventInfoProfile = new TestPureEventInfo[Event](
          event = mockEvent
        )

        an [AssertionError] should be thrownBy pureEventInfoProfile.toWatchpointEvent
      }

      it("should return a new copy of WatchpointEventProfile using the event producer") {
        val expected = mock[WatchpointEventInfo]

        val mockWatchpointEvent = mock[WatchpointEvent]
        val pureEventInfoProfile = new TestPureEventInfo[WatchpointEvent](
          event = mockWatchpointEvent
        )

        // Acquires access to the event info producer
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()

        // Creates a new instance of the event info using defaults
        (mockEventInfoProducer.newDefaultWatchpointEventInfo _).expects(
          mockScalaVirtualMachine,
          mockWatchpointEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureEventInfoProfile.toWatchpointEvent

        actual should be (expected)
      }
    }
  }
}
