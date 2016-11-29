package org.scaladebugger.api.profiles.traits.requests.methods
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.methods.MethodExitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfoProfile

import scala.util.{Failure, Success, Try}

class MethodExitProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MethodExitProfile#MethodExitEventAndData]
  )

  private val successMethodExitProfile = new Object with MethodExitProfile {
    override def tryGetOrCreateMethodExitRequestWithData(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MethodExitEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeMethodExitRequests(
      className: String,
      methodName: String
    ): Seq[MethodExitRequestInfo] = ???
    override def removeMethodExitRequestWithArgs(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Option[MethodExitRequestInfo] = ???
    override def removeAllMethodExitRequests(): Seq[MethodExitRequestInfo] = ???
    override def isMethodExitRequestPending(
      className: String,
      methodName: String
    ): Boolean = ???
    override def isMethodExitRequestWithArgsPending(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def methodExitRequests: Seq[MethodExitRequestInfo] = ???
  }

  private val failMethodExitProfile = new Object with MethodExitProfile {
    override def tryGetOrCreateMethodExitRequestWithData(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MethodExitEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeMethodExitRequests(
      className: String,
      methodName: String
    ): Seq[MethodExitRequestInfo] = ???
    override def removeMethodExitRequestWithArgs(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Option[MethodExitRequestInfo] = ???
    override def removeAllMethodExitRequests(): Seq[MethodExitRequestInfo] = ???
    override def isMethodExitRequestPending(
      className: String,
      methodName: String
    ): Boolean = ???
    override def isMethodExitRequestWithArgsPending(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def methodExitRequests: Seq[MethodExitRequestInfo] = ???
  }

  describe("MethodExitProfile") {
    describe("#tryGetOrCreateMethodExitRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MethodExitEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MethodExitEventInfoProfile = null
        successMethodExitProfile.tryGetOrCreateMethodExitRequest("", "").get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[MethodExitEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failMethodExitProfile.tryGetOrCreateMethodExitRequest("", "").failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateMethodExitRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MethodExitEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MethodExitEventInfoProfile = null
        successMethodExitProfile
          .getOrCreateMethodExitRequest("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMethodExitProfile.getOrCreateMethodExitRequest("", "")
        }
      }
    }

    describe("#getOrCreateMethodExitRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MethodExitEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (MethodExitEventInfoProfile, Seq[JDIEventDataResult]) = null
        successMethodExitProfile
          .getOrCreateMethodExitRequestWithData("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMethodExitProfile.getOrCreateMethodExitRequestWithData("", "")
        }
      }
    }
  }
}

