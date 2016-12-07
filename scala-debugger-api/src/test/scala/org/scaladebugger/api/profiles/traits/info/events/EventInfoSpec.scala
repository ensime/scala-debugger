package org.scaladebugger.api.profiles.traits.info.events

import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.EventInfoTestClasses.TestEventInfo

class EventInfoSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])

  describe("EventInfo") {
    describe("#allArguments") {
      it("Should return the combination of request and event jdi arguments") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def requestArguments: Seq[JDIRequestArgument] = mockJdiRequestArguments
          override def eventArguments: Seq[JDIEventArgument] = mockJdiEventArguments
        }

        val args = mockJdiRequestArguments ++ mockJdiEventArguments
        eventInfoProfile.allArguments should contain theSameElementsAs args
      }
    }

    describe("#isMonitorEvent") {
      it("should return true if a monitor contended entered event") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnteredEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor contended enter event") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnterEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor waited event") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitedEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor wait event") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return false if not a monitor event") {
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        )

        eventInfoProfile.isMonitorEvent should be (false)
      }
    }

    describe("#toMostSpecificEvent") {
      it("should invoke #toAccessWatchpointEvent if appropriate") {
        val expected = mock[AccessWatchpointEventInfo]

        val mockToEvent = mockFunction[AccessWatchpointEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isAccessWatchpointEvent: Boolean = true
          override def toAccessWatchpointEvent: AccessWatchpointEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toBreakpointEvent if appropriate") {
        val expected = mock[BreakpointEventInfo]

        val mockToEvent = mockFunction[BreakpointEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isBreakpointEvent: Boolean = true
          override def toBreakpointEvent: BreakpointEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toClassPrepareEvent if appropriate") {
        val expected = mock[ClassPrepareEventInfo]

        val mockToEvent = mockFunction[ClassPrepareEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isClassPrepareEvent: Boolean = true
          override def toClassPrepareEvent: ClassPrepareEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toClassUnloadEvent if appropriate") {
        val expected = mock[ClassUnloadEventInfo]

        val mockToEvent = mockFunction[ClassUnloadEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isClassUnloadEvent: Boolean = true
          override def toClassUnloadEvent: ClassUnloadEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toExceptionEvent if appropriate") {
        val expected = mock[ExceptionEventInfo]

        val mockToEvent = mockFunction[ExceptionEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isExceptionEvent: Boolean = true
          override def toExceptionEvent: ExceptionEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toLocatableEvent if appropriate") {
        val expected = mock[LocatableEventInfo]

        val mockToEvent = mockFunction[LocatableEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isLocatableEvent: Boolean = true
          override def toLocatableEvent: LocatableEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMethodEntryEvent if appropriate") {
        val expected = mock[MethodEntryEventInfo]

        val mockToEvent = mockFunction[MethodEntryEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMethodEntryEvent: Boolean = true
          override def toMethodEntryEvent: MethodEntryEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMethodExitEvent if appropriate") {
        val expected = mock[MethodExitEventInfo]

        val mockToEvent = mockFunction[MethodExitEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMethodExitEvent: Boolean = true
          override def toMethodExitEvent: MethodExitEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toModificationWatchpointEvent if appropriate") {
        val expected = mock[ModificationWatchpointEventInfo]

        val mockToEvent = mockFunction[ModificationWatchpointEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isModificationWatchpointEvent: Boolean = true
          override def toModificationWatchpointEvent: ModificationWatchpointEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorEvent if appropriate") {
        val expected = mock[MonitorEventInfo]

        val mockToEvent = mockFunction[MonitorEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorEvent: Boolean = true
          override def toMonitorEvent: MonitorEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorContendedEnteredEvent if appropriate") {
        val expected = mock[MonitorContendedEnteredEventInfo]

        val mockToEvent = mockFunction[MonitorContendedEnteredEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnteredEvent: Boolean = true
          override def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorContendedEnterEvent if appropriate") {
        val expected = mock[MonitorContendedEnterEventInfo]

        val mockToEvent = mockFunction[MonitorContendedEnterEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnterEvent: Boolean = true
          override def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorWaitedEvent if appropriate") {
        val expected = mock[MonitorWaitedEventInfo]

        val mockToEvent = mockFunction[MonitorWaitedEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitedEvent: Boolean = true
          override def toMonitorWaitedEvent: MonitorWaitedEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorWaitEvent if appropriate") {
        val expected = mock[MonitorWaitEventInfo]

        val mockToEvent = mockFunction[MonitorWaitEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitEvent: Boolean = true
          override def toMonitorWaitEvent: MonitorWaitEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toStepEvent if appropriate") {
        val expected = mock[StepEventInfo]

        val mockToEvent = mockFunction[StepEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isStepEvent: Boolean = true
          override def toStepEvent: StepEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toThreadDeathEvent if appropriate") {
        val expected = mock[ThreadDeathEventInfo]

        val mockToEvent = mockFunction[ThreadDeathEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isThreadDeathEvent: Boolean = true
          override def toThreadDeathEvent: ThreadDeathEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toThreadStartEvent if appropriate") {
        val expected = mock[ThreadStartEventInfo]

        val mockToEvent = mockFunction[ThreadStartEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isThreadStartEvent: Boolean = true
          override def toThreadStartEvent: ThreadStartEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMDeathEvent if appropriate") {
        val expected = mock[VMDeathEventInfo]

        val mockToEvent = mockFunction[VMDeathEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMDeathEvent: Boolean = true
          override def toVMDeathEvent: VMDeathEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMDisconnectEvent if appropriate") {
        val expected = mock[VMDisconnectEventInfo]

        val mockToEvent = mockFunction[VMDisconnectEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMDisconnectEvent: Boolean = true
          override def toVMDisconnectEvent: VMDisconnectEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMStartEvent if appropriate") {
        val expected = mock[VMStartEventInfo]

        val mockToEvent = mockFunction[VMStartEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMStartEvent: Boolean = true
          override def toVMStartEvent: VMStartEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toWatchpointEvent if appropriate") {
        val expected = mock[WatchpointEventInfo]

        val mockToEvent = mockFunction[WatchpointEventInfo]
        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isWatchpointEvent: Boolean = true
          override def toWatchpointEvent: WatchpointEventInfo =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should do nothing if no specific event is appropriate") {
        val expected = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        )

        val actual = expected.toMostSpecificEvent

        actual should be (expected)
      }
    }

    describe("#toPrettyString") {
      it("should invoke toPrettyString on most specific event if it is not plain") {
        val expected = "some pretty string"
        val mockEventInfoProfile = mock[EventInfo]

        // Indicate not plain
        (mockEventInfoProfile.isPlainEvent _).expects()
          .returning(false).once()

        (mockEventInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfo =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should invoke toString if most specific event is plain") {
        val mockEventInfoProfile = mock[EventInfo]
        val expected = mockEventInfoProfile.toString

        // Indicate is plain
        (mockEventInfoProfile.isPlainEvent _).expects()
          .returning(true).once()

        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfo =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should return <ERROR> if an exception was thrown") {
        val expected = "<ERROR>"
        val mockEventInfoProfile = mock[EventInfo]

        // Force throw an error
        (mockEventInfoProfile.isPlainEvent _).expects()
          .throwing(new Throwable).once()

        val eventInfoProfile = new TestEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfo =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }
    }
  }
}
