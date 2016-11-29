package org.scaladebugger.api.profiles.traits.requests.monitors
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnteredRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnteredEventInfoProfile

import scala.util.{Failure, Success, Try}

class MonitorContendedEnteredProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MonitorContendedEnteredProfile#MonitorContendedEnteredEventAndData]
  )

  private val successMonitorContendedEnteredProfile = new Object with MonitorContendedEnteredProfile {
    override def tryGetOrCreateMonitorContendedEnteredRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeMonitorContendedEnteredRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorContendedEnteredRequestInfo] = ???

    override def removeAllMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo] = ???

    override def isMonitorContendedEnteredRequestWithArgsPending(extraArguments: JDIArgument*): Boolean = ???

    override def monitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo] = ???
  }

  private val failMonitorContendedEnteredProfile = new Object with MonitorContendedEnteredProfile {
    override def tryGetOrCreateMonitorContendedEnteredRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeMonitorContendedEnteredRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[MonitorContendedEnteredRequestInfo] = ???

    override def removeAllMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo] = ???

    override def isMonitorContendedEnteredRequestWithArgsPending(extraArguments: JDIArgument*): Boolean = ???

    override def monitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo] = ???
  }

  describe("MonitorContendedEnteredProfile") {
    describe("#tryGetOrCreateMonitorContendedEnteredRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorContendedEnteredEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorContendedEnteredEventInfoProfile = null
        successMonitorContendedEnteredProfile
          .tryGetOrCreateMonitorContendedEnteredRequest()
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
        failMonitorContendedEnteredProfile
          .tryGetOrCreateMonitorContendedEnteredRequest()
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateMonitorContendedEnteredRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MonitorContendedEnteredEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorContendedEnteredEventInfoProfile = null
        successMonitorContendedEnteredProfile
          .getOrCreateMonitorContendedEnteredRequest()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorContendedEnteredProfile.getOrCreateMonitorContendedEnteredRequest()
        }
      }
    }

    describe("#getOrCreateMonitorContendedEnteredRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MonitorContendedEnteredEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (MonitorContendedEnteredEventInfoProfile, Seq[JDIEventDataResult]) = null
        successMonitorContendedEnteredProfile
          .getOrCreateMonitorContendedEnteredRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorContendedEnteredProfile
            .getOrCreateMonitorContendedEnteredRequestWithData()
        }
      }
    }
  }
}

