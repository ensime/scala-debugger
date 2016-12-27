package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import EventType._
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class EventTypeSpec extends ParallelMockFunSpec {
  describe("EventType") {
    describe("#eventToEventType") {
      it("should convert a VMStartEvent to Some(VMStartEventType)") {
        EventType.eventToEventType(mock[VMStartEvent]) should
          be (Some(VMStartEventType))
      }

      it("should convert a VMDeathEvent to Some(VMDeathEventType)") {
        EventType.eventToEventType(mock[VMDeathEvent]) should
          be (Some(VMDeathEventType))
      }

      it("should convert a ThreadStartEvent to Some(ThreadStartEventType)") {
        EventType.eventToEventType(mock[ThreadStartEvent]) should
          be (Some(ThreadStartEventType))
      }

      it("should convert a ThreadDeathEvent to Some(ThreadDeathEventType)") {
        EventType.eventToEventType(mock[ThreadDeathEvent]) should
          be (Some(ThreadDeathEventType))
      }

      it("should convert a ClassPrepareEvent to Some(ClassPrepareEventType)") {
        EventType.eventToEventType(mock[ClassPrepareEvent]) should
          be (Some(ClassPrepareEventType))
      }

      it("should convert a ClassUnloadEvent to Some(ClassUnloadEventType)") {
        EventType.eventToEventType(mock[ClassUnloadEvent]) should
          be (Some(ClassUnloadEventType))
      }

      it("should convert an AccessWatchpointEvent to Some(AccessWatchpointEventType)") {
        EventType.eventToEventType(mock[AccessWatchpointEvent]) should
          be (Some(AccessWatchpointEventType))
      }

      it("should convert a ModificationWatchpointEvent to Some(ModificationWatchpointEventType)") {
        EventType.eventToEventType(mock[ModificationWatchpointEvent]) should
          be (Some(ModificationWatchpointEventType))
      }

      it("should convert a MonitorContendedEnteredEvent to Some(MonitorContendedEnteredEventType)") {
        EventType.eventToEventType(mock[MonitorContendedEnteredEvent]) should
          be (Some(MonitorContendedEnteredEventType))
      }

      it("should convert a MonitorContendedEnterEvent to Some(MonitorContendedEnterEventType)") {
        EventType.eventToEventType(mock[MonitorContendedEnterEvent]) should
          be (Some(MonitorContendedEnterEventType))
      }

      it("should convert a MonitorWaitedEvent to Some(MonitorWaitedEventType)") {
        EventType.eventToEventType(mock[MonitorWaitedEvent]) should
          be (Some(MonitorWaitedEventType))
      }

      it("should convert a MonitorWaitEvent to Some(MonitorWaitEventType)") {
        EventType.eventToEventType(mock[MonitorWaitEvent]) should
          be (Some(MonitorWaitEventType))
      }

      it("should convert an ExceptionEvent to Some(ExceptionEventType)") {
        EventType.eventToEventType(mock[ExceptionEvent]) should
          be (Some(ExceptionEventType))
      }

      it("should convert a MethodEntryEvent to Some(MethodEntryEventType)") {
        EventType.eventToEventType(mock[MethodEntryEvent]) should
          be (Some(MethodEntryEventType))
      }

      it("should convert a MethodExitEvent to Some(MethodExitEventType)") {
        EventType.eventToEventType(mock[MethodExitEvent]) should
          be (Some(MethodExitEventType))
      }

      it("should convert a BreakpointEvent to Some(BreakpointEventType)") {
        EventType.eventToEventType(mock[BreakpointEvent]) should
          be (Some(BreakpointEventType))
      }

      it("should convert a StepEvent to Some(StepEventType)") {
        EventType.eventToEventType(mock[StepEvent]) should
          be (Some(StepEventType))
      }

      it("should return None if an unknown event type") {
        EventType.eventToEventType(mock[Event]) should be (None)
      }
    }

    describe("#eventClassToEventType") {
      it("should convert a VMStartEvent class to Some(VMStartEventType)") {
        EventType.eventClassToEventType(classOf[VMStartEvent]) should
          be (Some(VMStartEventType))
      }

      it("should convert a VMDeathEvent class to Some(VMDeathEventType)") {
        EventType.eventClassToEventType(classOf[VMDeathEvent]) should
          be (Some(VMDeathEventType))
      }

      it("should convert a ThreadStartEvent class to Some(ThreadStartEventType)") {
        EventType.eventClassToEventType(classOf[ThreadStartEvent]) should
          be (Some(ThreadStartEventType))
      }

      it("should convert a ThreadDeathEvent class to Some(ThreadDeathEventType)") {
        EventType.eventClassToEventType(classOf[ThreadDeathEvent]) should
          be (Some(ThreadDeathEventType))
      }

      it("should convert a ClassPrepareEvent class to Some(ClassPrepareEventType)") {
        EventType.eventClassToEventType(classOf[ClassPrepareEvent]) should
          be (Some(ClassPrepareEventType))
      }

      it("should convert a ClassUnloadEvent class to Some(ClassUnloadEventType)") {
        EventType.eventClassToEventType(classOf[ClassUnloadEvent]) should
          be (Some(ClassUnloadEventType))
      }

      it("should convert an AccessWatchpointEvent class to Some(AccessWatchpointEventType)") {
        EventType.eventClassToEventType(classOf[AccessWatchpointEvent]) should
          be (Some(AccessWatchpointEventType))
      }

      it("should convert a ModificationWatchpointEvent class to Some(ModificationWatchpointEventType)") {
        EventType.eventClassToEventType(classOf[ModificationWatchpointEvent]) should
          be (Some(ModificationWatchpointEventType))
      }

      it("should convert a MonitorContendedEnteredEvent class to Some(MonitorContendedEnteredEventType)") {
        EventType.eventClassToEventType(classOf[MonitorContendedEnteredEvent]) should
          be (Some(MonitorContendedEnteredEventType))
      }

      it("should convert a MonitorContendedEnterEvent class to Some(MonitorContendedEnterEventType)") {
        EventType.eventClassToEventType(classOf[MonitorContendedEnterEvent]) should
          be (Some(MonitorContendedEnterEventType))
      }

      it("should convert a MonitorWaitedEvent class to Some(MonitorWaitedEventType)") {
        EventType.eventClassToEventType(classOf[MonitorWaitedEvent]) should
          be (Some(MonitorWaitedEventType))
      }

      it("should convert a MonitorWaitEvent class to Some(MonitorWaitEventType)") {
        EventType.eventClassToEventType(classOf[MonitorWaitEvent]) should
          be (Some(MonitorWaitEventType))
      }

      it("should convert an ExceptionEvent class to Some(ExceptionEventType)") {
        EventType.eventClassToEventType(classOf[ExceptionEvent]) should
          be (Some(ExceptionEventType))
      }

      it("should convert a MethodEntryEvent class to Some(MethodEntryEventType)") {
        EventType.eventClassToEventType(classOf[MethodEntryEvent]) should
          be (Some(MethodEntryEventType))
      }

      it("should convert a MethodExitEvent class to Some(MethodExitEventType)") {
        EventType.eventClassToEventType(classOf[MethodExitEvent]) should
          be (Some(MethodExitEventType))
      }

      it("should convert a BreakpointEvent class to Some(BreakpointEventType)") {
        EventType.eventClassToEventType(classOf[BreakpointEvent]) should
          be (Some(BreakpointEventType))
      }

      it("should convert a StepEvent class to Some(StepEventType)") {
        EventType.eventClassToEventType(classOf[StepEvent]) should
          be (Some(StepEventType))
      }

      it("should return None if an unknown event type") {
        EventType.eventClassToEventType(classOf[Event]) should be (None)
      }
    }
  }
}
