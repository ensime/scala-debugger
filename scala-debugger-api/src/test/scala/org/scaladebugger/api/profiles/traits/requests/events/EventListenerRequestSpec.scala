package org.scaladebugger.api.profiles.traits.requests.events
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventHandlerInfo
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.EventInfo

import scala.util.{Failure, Success, Try}

class EventListenerRequestSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[EventListenerRequest#EventAndData]
  )

  private val successEventListenerProfile = new Object with EventListenerRequest {
    override def tryCreateEventListenerWithData(
      eventType: EventType,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[EventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def eventHandlers: Seq[EventHandlerInfo] = ???
  }

  private val failEventListenerProfile = new Object with EventListenerRequest {
    override def tryCreateEventListenerWithData(
      eventType: EventType,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[EventAndData]] = {
      Failure(TestThrowable)
    }

    override def eventHandlers: Seq[EventHandlerInfo] = ???
  }

  describe("EventListenerRequest") {
    describe("#tryCreateEventListener") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[EventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: EventInfo = null
        successEventListenerProfile.tryCreateEventListener(mock[EventType]).get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[EventInfo], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failEventListenerProfile.tryCreateEventListener(mock[EventType]).failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#createEventListener") {
      it("should return a pipeline of events if successful") {
        val expected = mock[EventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: EventInfo = null
        successEventListenerProfile.createEventListener(mock[EventType]).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failEventListenerProfile.createEventListener(mock[EventType])
        }
      }
    }

    describe("#createEventListenerWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[EventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (EventInfo, Seq[JDIEventDataResult]) = null
        successEventListenerProfile
          .createEventListenerWithData(mock[EventType])
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failEventListenerProfile.createEventListenerWithData(mock[EventType])
        }
      }
    }
  }
}

