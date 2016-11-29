package org.scaladebugger.api.profiles.traits.info.events

import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.EventInfoTestClasses.TestEventInfoProfile

class EventInfoProfileSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])

  describe("EventInfoProfile") {
    describe("#allArguments") {
      it("Should return the combination of request and event jdi arguments") {
        val eventInfoProfile = new TestEventInfoProfile(
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
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnteredEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor contended enter event") {
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnterEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor waited event") {
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitedEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return true if a monitor wait event") {
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitEvent: Boolean = true
        }

        eventInfoProfile.isMonitorEvent should be (true)
      }

      it("should return false if not a monitor event") {
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        )

        eventInfoProfile.isMonitorEvent should be (false)
      }
    }

    describe("#toMostSpecificEvent") {
      it("should invoke #toAccessWatchpointEvent if appropriate") {
        val expected = mock[AccessWatchpointEventInfoProfile]

        val mockToEvent = mockFunction[AccessWatchpointEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isAccessWatchpointEvent: Boolean = true
          override def toAccessWatchpointEvent: AccessWatchpointEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toBreakpointEvent if appropriate") {
        val expected = mock[BreakpointEventInfoProfile]

        val mockToEvent = mockFunction[BreakpointEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isBreakpointEvent: Boolean = true
          override def toBreakpointEvent: BreakpointEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toClassPrepareEvent if appropriate") {
        val expected = mock[ClassPrepareEventInfoProfile]

        val mockToEvent = mockFunction[ClassPrepareEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isClassPrepareEvent: Boolean = true
          override def toClassPrepareEvent: ClassPrepareEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toClassUnloadEvent if appropriate") {
        val expected = mock[ClassUnloadEventInfoProfile]

        val mockToEvent = mockFunction[ClassUnloadEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isClassUnloadEvent: Boolean = true
          override def toClassUnloadEvent: ClassUnloadEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toExceptionEvent if appropriate") {
        val expected = mock[ExceptionEventInfoProfile]

        val mockToEvent = mockFunction[ExceptionEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isExceptionEvent: Boolean = true
          override def toExceptionEvent: ExceptionEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toLocatableEvent if appropriate") {
        val expected = mock[LocatableEventInfoProfile]

        val mockToEvent = mockFunction[LocatableEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isLocatableEvent: Boolean = true
          override def toLocatableEvent: LocatableEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMethodEntryEvent if appropriate") {
        val expected = mock[MethodEntryEventInfoProfile]

        val mockToEvent = mockFunction[MethodEntryEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMethodEntryEvent: Boolean = true
          override def toMethodEntryEvent: MethodEntryEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMethodExitEvent if appropriate") {
        val expected = mock[MethodExitEventInfoProfile]

        val mockToEvent = mockFunction[MethodExitEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMethodExitEvent: Boolean = true
          override def toMethodExitEvent: MethodExitEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toModificationWatchpointEvent if appropriate") {
        val expected = mock[ModificationWatchpointEventInfoProfile]

        val mockToEvent = mockFunction[ModificationWatchpointEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isModificationWatchpointEvent: Boolean = true
          override def toModificationWatchpointEvent: ModificationWatchpointEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorEvent if appropriate") {
        val expected = mock[MonitorEventInfoProfile]

        val mockToEvent = mockFunction[MonitorEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorEvent: Boolean = true
          override def toMonitorEvent: MonitorEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorContendedEnteredEvent if appropriate") {
        val expected = mock[MonitorContendedEnteredEventInfoProfile]

        val mockToEvent = mockFunction[MonitorContendedEnteredEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnteredEvent: Boolean = true
          override def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorContendedEnterEvent if appropriate") {
        val expected = mock[MonitorContendedEnterEventInfoProfile]

        val mockToEvent = mockFunction[MonitorContendedEnterEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorContendedEnterEvent: Boolean = true
          override def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorWaitedEvent if appropriate") {
        val expected = mock[MonitorWaitedEventInfoProfile]

        val mockToEvent = mockFunction[MonitorWaitedEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitedEvent: Boolean = true
          override def toMonitorWaitedEvent: MonitorWaitedEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toMonitorWaitEvent if appropriate") {
        val expected = mock[MonitorWaitEventInfoProfile]

        val mockToEvent = mockFunction[MonitorWaitEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isMonitorWaitEvent: Boolean = true
          override def toMonitorWaitEvent: MonitorWaitEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toStepEvent if appropriate") {
        val expected = mock[StepEventInfoProfile]

        val mockToEvent = mockFunction[StepEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isStepEvent: Boolean = true
          override def toStepEvent: StepEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toThreadDeathEvent if appropriate") {
        val expected = mock[ThreadDeathEventInfoProfile]

        val mockToEvent = mockFunction[ThreadDeathEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isThreadDeathEvent: Boolean = true
          override def toThreadDeathEvent: ThreadDeathEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toThreadStartEvent if appropriate") {
        val expected = mock[ThreadStartEventInfoProfile]

        val mockToEvent = mockFunction[ThreadStartEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isThreadStartEvent: Boolean = true
          override def toThreadStartEvent: ThreadStartEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMDeathEvent if appropriate") {
        val expected = mock[VMDeathEventInfoProfile]

        val mockToEvent = mockFunction[VMDeathEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMDeathEvent: Boolean = true
          override def toVMDeathEvent: VMDeathEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMDisconnectEvent if appropriate") {
        val expected = mock[VMDisconnectEventInfoProfile]

        val mockToEvent = mockFunction[VMDisconnectEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMDisconnectEvent: Boolean = true
          override def toVMDisconnectEvent: VMDisconnectEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toVMStartEvent if appropriate") {
        val expected = mock[VMStartEventInfoProfile]

        val mockToEvent = mockFunction[VMStartEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isVMStartEvent: Boolean = true
          override def toVMStartEvent: VMStartEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should invoke #toWatchpointEvent if appropriate") {
        val expected = mock[WatchpointEventInfoProfile]

        val mockToEvent = mockFunction[WatchpointEventInfoProfile]
        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def isWatchpointEvent: Boolean = true
          override def toWatchpointEvent: WatchpointEventInfoProfile =
            mockToEvent()
        }

        mockToEvent.expects().returning(expected).once()

        val actual = eventInfoProfile.toMostSpecificEvent

        actual should be (expected)
      }

      it("should do nothing if no specific event is appropriate") {
        val expected = new TestEventInfoProfile(
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
        val mockEventInfoProfile = mock[EventInfoProfile]

        // Indicate not plain
        (mockEventInfoProfile.isPlainEvent _).expects()
          .returning(false).once()

        (mockEventInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfoProfile =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should invoke toString if most specific event is plain") {
        val mockEventInfoProfile = mock[EventInfoProfile]
        val expected = mockEventInfoProfile.toString

        // Indicate is plain
        (mockEventInfoProfile.isPlainEvent _).expects()
          .returning(true).once()

        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfoProfile =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should return <ERROR> if an exception was thrown") {
        val expected = "<ERROR>"
        val mockEventInfoProfile = mock[EventInfoProfile]

        // Force throw an error
        (mockEventInfoProfile.isPlainEvent _).expects()
          .throwing(new Throwable).once()

        val eventInfoProfile = new TestEventInfoProfile(
          scalaVirtualMachine = mockScalaVirtualMachine,
          isJavaInfo = false
        ) {
          override def toMostSpecificEvent: EventInfoProfile =
            mockEventInfoProfile
        }

        val actual = eventInfoProfile.toPrettyString

        actual should be (expected)
      }
    }
  }
}
