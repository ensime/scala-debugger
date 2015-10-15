package org.senkbeil.debugger.api.events

import com.sun.jdi.event._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FunSpec}

import EventType._

class EventTypeSpec extends FunSpec with Matchers with MockFactory {
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
  }
}
