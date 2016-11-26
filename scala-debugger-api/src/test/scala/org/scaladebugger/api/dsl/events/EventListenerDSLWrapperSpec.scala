package org.scaladebugger.api.dsl.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.events.EventListenerProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class EventListenerDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockEventProfile = mock[EventListenerProfile]

  describe("EventListenerDSLWrapper") {
    describe("#onEvent") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.EventListenerDSL

        // NOTE: Cannot mock EventType (get stack overflow error)
        val eventType = EventType.BreakpointEventType
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[Event]))

        (mockEventProfile.tryCreateEventListener _).expects(
          eventType,
          extraArguments
        ).returning(returnValue).once()

        mockEventProfile.onEvent(
          eventType,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeEvent") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.EventListenerDSL

        // NOTE: Cannot mock EventType (get stack overflow error)
        val eventType = EventType.BreakpointEventType
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[Event])

        (mockEventProfile.createEventListener _).expects(
          eventType,
          extraArguments
        ).returning(returnValue).once()

        mockEventProfile.onUnsafeEvent(
          eventType,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onEventWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.EventListenerDSL

        // NOTE: Cannot mock EventType (get stack overflow error)
        val eventType = EventType.BreakpointEventType
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        ))

        (mockEventProfile.tryCreateEventListenerWithData _).expects(
          eventType,
          extraArguments
        ).returning(returnValue).once()

        mockEventProfile.onEventWithData(
          eventType,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeEventWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.EventListenerDSL

        // NOTE: Cannot mock EventType (get stack overflow error)
        val eventType = EventType.BreakpointEventType
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )

        (mockEventProfile.createEventListenerWithData _).expects(
          eventType,
          extraArguments
        ).returning(returnValue).once()

        mockEventProfile.onUnsafeEventWithData(
          eventType,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
