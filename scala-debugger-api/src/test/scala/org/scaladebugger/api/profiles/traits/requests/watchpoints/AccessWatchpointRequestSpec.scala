package org.scaladebugger.api.profiles.traits.requests.watchpoints
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.watchpoints.AccessWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.AccessWatchpointEventInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success, Try}

class AccessWatchpointRequestSpec extends ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[AccessWatchpointRequest#AccessWatchpointEventAndData]
  )

  private val successAccessWatchpointProfile = new Object with AccessWatchpointRequest {
    override def tryGetOrCreateAccessWatchpointRequestWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeAccessWatchpointRequests(
      className: String,
      fieldName: String
    ): Seq[AccessWatchpointRequestInfo] = ???
    override def removeAccessWatchpointRequestWithArgs(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Option[AccessWatchpointRequestInfo] = ???
    override def removeAllAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo] = ???
    override def isAccessWatchpointRequestPending(
      className: String,
      fieldName: String
    ): Boolean = ???
    override def isAccessWatchpointRequestWithArgsPending(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = ???
  }

  private val failAccessWatchpointProfile = new Object with AccessWatchpointRequest {
    override def tryGetOrCreateAccessWatchpointRequestWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeAccessWatchpointRequests(
      className: String,
      fieldName: String
    ): Seq[AccessWatchpointRequestInfo] = ???
    override def removeAccessWatchpointRequestWithArgs(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Option[AccessWatchpointRequestInfo] = ???
    override def removeAllAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo] = ???
    override def isAccessWatchpointRequestPending(
      className: String,
      fieldName: String
    ): Boolean = ???
    override def isAccessWatchpointRequestWithArgsPending(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = ???
  }

  describe("AccessWatchpointRequest") {
    describe("#tryGetOrCreateAccessWatchpointRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[AccessWatchpointEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: AccessWatchpointEventInfo = null
        successAccessWatchpointProfile
          .tryGetOrCreateAccessWatchpointRequest("", "")
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any error as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failAccessWatchpointProfile
          .tryGetOrCreateAccessWatchpointRequest("", "")
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateAccessWatchpointRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[AccessWatchpointEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: AccessWatchpointEventInfo = null
        successAccessWatchpointProfile
          .getOrCreateAccessWatchpointRequest("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failAccessWatchpointProfile.getOrCreateAccessWatchpointRequest("", "")
        }
      }
    }

    describe("#getOrCreateAccessWatchpointRequestWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[AccessWatchpointEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (AccessWatchpointEventInfo, Seq[JDIEventDataResult]) = null
        successAccessWatchpointProfile
          .getOrCreateAccessWatchpointRequestWithData("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failAccessWatchpointProfile
            .getOrCreateAccessWatchpointRequestWithData("", "")
        }
      }
    }
  }
}

