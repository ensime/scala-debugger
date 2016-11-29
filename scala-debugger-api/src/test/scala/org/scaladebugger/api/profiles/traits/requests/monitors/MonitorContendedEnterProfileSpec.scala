package org.scaladebugger.api.profiles.traits.requests.monitors
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnterRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnterEventInfoProfile

import scala.util.{Failure, Success, Try}

class MonitorContendedEnterProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MonitorContendedEnterProfile#MonitorContendedEnterEventAndData]
  )

  private val successMonitorContendedEnterProfile = new Object with MonitorContendedEnterProfile {
    override def tryGetOrCreateMonitorContendedEnterRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeMonitorContendedEnterRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorContendedEnterRequestInfo] = ???

    override def removeAllMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo] = ???

    override def isMonitorContendedEnterRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = ???
  }

  private val failMonitorContendedEnterProfile = new Object with MonitorContendedEnterProfile {
    override def tryGetOrCreateMonitorContendedEnterRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeMonitorContendedEnterRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorContendedEnterRequestInfo] = ???

    override def removeAllMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo] = ???

    override def isMonitorContendedEnterRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = ???
  }

  describe("MonitorContendedEnterProfile") {
    describe("#tryGetOrCreateMonitorContendedEnterRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorContendedEnterEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorContendedEnterEventInfoProfile = null
        successMonitorContendedEnterProfile
          .tryGetOrCreateMonitorContendedEnterRequest()
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
        failMonitorContendedEnterProfile
          .tryGetOrCreateMonitorContendedEnterRequest()
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateMonitorContendedEnterRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MonitorContendedEnterEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorContendedEnterEventInfoProfile = null
        successMonitorContendedEnterProfile
          .getOrCreateMonitorContendedEnterRequest()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorContendedEnterProfile.getOrCreateMonitorContendedEnterRequest()
        }
      }
    }

    describe("#getOrCreateMonitorContendedEnterRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MonitorContendedEnterEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (MonitorContendedEnterEventInfoProfile, Seq[JDIEventDataResult]) = null
        successMonitorContendedEnterProfile
          .getOrCreateMonitorContendedEnterRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorContendedEnterProfile
            .getOrCreateMonitorContendedEnterRequestWithData()
        }
      }
    }
  }
}
