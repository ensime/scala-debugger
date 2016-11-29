package org.scaladebugger.api.profiles.traits.requests.monitors
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorWaitedRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfoProfile

import scala.util.{Failure, Success, Try}

class MonitorWaitedProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MonitorWaitedProfile#MonitorWaitedEventAndData]
  )

  private val successMonitorWaitedProfile = new Object with MonitorWaitedProfile {
    override def tryGetOrCreateMonitorWaitedRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeMonitorWaitedRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorWaitedRequestInfo] = ???

    override def removeAllMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo] = ???

    override def isMonitorWaitedRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def monitorWaitedRequests: Seq[MonitorWaitedRequestInfo] = ???
  }

  private val failMonitorWaitedProfile = new Object with MonitorWaitedProfile {
    override def tryGetOrCreateMonitorWaitedRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeMonitorWaitedRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorWaitedRequestInfo] = ???

    override def removeAllMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo] = ???

    override def isMonitorWaitedRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def monitorWaitedRequests: Seq[MonitorWaitedRequestInfo] = ???
  }

  describe("MonitorWaitedProfile") {
    describe("#tryGetOrCreateMonitorWaitedRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorWaitedEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorWaitedEventInfoProfile = null
        successMonitorWaitedProfile
          .tryGetOrCreateMonitorWaitedRequest()
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failMonitorWaitedProfile
          .tryGetOrCreateMonitorWaitedRequest()
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateMonitorWaitedRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MonitorWaitedEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorWaitedEventInfoProfile = null
        successMonitorWaitedProfile
          .getOrCreateMonitorWaitedRequest()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorWaitedProfile.getOrCreateMonitorWaitedRequest()
        }
      }
    }

    describe("#getOrCreateMonitorWaitedRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MonitorWaitedEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (MonitorWaitedEventInfoProfile, Seq[JDIEventDataResult]) = null
        successMonitorWaitedProfile
          .getOrCreateMonitorWaitedRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorWaitedProfile.getOrCreateMonitorWaitedRequestWithData()
        }
      }
    }
  }
}

