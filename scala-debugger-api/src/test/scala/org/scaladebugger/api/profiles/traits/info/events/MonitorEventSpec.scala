package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.{Location, ObjectReference, ThreadReference, VirtualMachine}
import com.sun.jdi.event._
import com.sun.jdi.request.EventRequest
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class MonitorEventSpec extends ParallelMockFunSpec {
  describe("MonitorEvent") {
    describe("#constructor") {
      it("should throw an exception if locatable is not a monitor event type") {
        intercept[IllegalArgumentException] {
          new MonitorEvent(mock[LocatableEvent])
        }
      }
    }

    describe("#monitor") {
      it("should return MonitorContendedEnteredEvent's monitor") {
        val expected = mock[ObjectReference]

        val mockMonitorContendedEnteredEvent = mock[MonitorContendedEnteredEvent]
        val monitorEvent = new MonitorEvent(mockMonitorContendedEnteredEvent)

        (mockMonitorContendedEnteredEvent.monitor _).expects()
          .returning(expected).once()

        val actual = monitorEvent.monitor()

        actual should be (expected)
      }

      it("should return MonitorContendedEnterEvent's monitor") {
        val expected = mock[ObjectReference]

        val mockMonitorContendedEnterEvent = mock[MonitorContendedEnterEvent]
        val monitorEvent = new MonitorEvent(mockMonitorContendedEnterEvent)

        (mockMonitorContendedEnterEvent.monitor _).expects()
          .returning(expected).once()

        val actual = monitorEvent.monitor()

        actual should be (expected)
      }

      it("should return MonitorWaitedEvent's monitor") {
        val expected = mock[ObjectReference]

        val mockMonitorWaitedEvent = mock[MonitorWaitedEvent]
        val monitorEvent = new MonitorEvent(mockMonitorWaitedEvent)

        (mockMonitorWaitedEvent.monitor _).expects()
          .returning(expected).once()

        val actual = monitorEvent.monitor()

        actual should be (expected)
      }

      it("should return MonitorWaitEvent's monitor") {
        val expected = mock[ObjectReference]

        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val monitorEvent = new MonitorEvent(mockMonitorWaitEvent)

        (mockMonitorWaitEvent.monitor _).expects()
          .returning(expected).once()

        val actual = monitorEvent.monitor()

        actual should be (expected)
      }
    }

    describe("#thread") {
      it("should return MonitorContendedEnteredEvent's thread") {
        val expected = mock[ThreadReference]

        val mockMonitorContendedEnteredEvent = mock[MonitorContendedEnteredEvent]
        val threadEvent = new MonitorEvent(mockMonitorContendedEnteredEvent)

        (mockMonitorContendedEnteredEvent.thread _).expects()
          .returning(expected).once()

        val actual = threadEvent.thread()

        actual should be (expected)
      }

      it("should return MonitorContendedEnterEvent's thread") {
        val expected = mock[ThreadReference]

        val mockMonitorContendedEnterEvent = mock[MonitorContendedEnterEvent]
        val threadEvent = new MonitorEvent(mockMonitorContendedEnterEvent)

        (mockMonitorContendedEnterEvent.thread _).expects()
          .returning(expected).once()

        val actual = threadEvent.thread()

        actual should be (expected)
      }

      it("should return MonitorWaitedEvent's thread") {
        val expected = mock[ThreadReference]

        val mockMonitorWaitedEvent = mock[MonitorWaitedEvent]
        val threadEvent = new MonitorEvent(mockMonitorWaitedEvent)

        (mockMonitorWaitedEvent.thread _).expects()
          .returning(expected).once()

        val actual = threadEvent.thread()

        actual should be (expected)
      }

      it("should return MonitorWaitEvent's thread") {
        val expected = mock[ThreadReference]

        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val threadEvent = new MonitorEvent(mockMonitorWaitEvent)

        (mockMonitorWaitEvent.thread _).expects()
          .returning(expected).once()

        val actual = threadEvent.thread()

        actual should be (expected)
      }
    }

    describe("#location") {
      it("should return the locatable's location") {
        val expected = mock[Location]

        // NOTE: Need to provide a valid event type
        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val threadEvent = new MonitorEvent(mockMonitorWaitEvent)

        (mockMonitorWaitEvent.location _).expects()
          .returning(expected).once()

        val actual = threadEvent.location()

        actual should be (expected)
      }
    }

    describe("#virtualMachine") {
      it("should return the locatable's virtual machine") {
        val expected = mock[VirtualMachine]

        // NOTE: Need to provide a valid event type
        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val threadEvent = new MonitorEvent(mockMonitorWaitEvent)

        (mockMonitorWaitEvent.virtualMachine _).expects()
          .returning(expected).once()

        val actual = threadEvent.virtualMachine()

        actual should be (expected)
      }
    }

    describe("#request") {
      it("should return the locatable's request") {
        val expected = mock[EventRequest]

        // NOTE: Need to provide a valid event type
        val mockMonitorWaitEvent = mock[MonitorWaitEvent]
        val threadEvent = new MonitorEvent(mockMonitorWaitEvent)

        (mockMonitorWaitEvent.request _).expects()
          .returning(expected).once()

        val actual = threadEvent.request()

        actual should be (expected)
      }
    }
  }
}
