package org.scaladebugger.api.profiles.traits.requests.methods
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.methods.MethodEntryRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfoProfile

import scala.util.{Failure, Success, Try}

class MethodEntryProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MethodEntryProfile#MethodEntryEventAndData]
  )

  private val successMethodEntryProfile = new Object with MethodEntryProfile {
    override def tryGetOrCreateMethodEntryRequestWithData(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeMethodEntryRequests(
      className: String,
      methodName: String
    ): Seq[MethodEntryRequestInfo] = ???
    override def removeMethodEntryRequestWithArgs(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Option[MethodEntryRequestInfo] = ???
    override def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo] = ???
    override def isMethodEntryRequestPending(
      className: String,
      methodName: String
    ): Boolean = ???
    override def isMethodEntryRequestWithArgsPending(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def methodEntryRequests: Seq[MethodEntryRequestInfo] = ???
  }

  private val failMethodEntryProfile = new Object with MethodEntryProfile {
    override def tryGetOrCreateMethodEntryRequestWithData(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeMethodEntryRequests(
      className: String,
      methodName: String
    ): Seq[MethodEntryRequestInfo] = ???
    override def removeMethodEntryRequestWithArgs(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Option[MethodEntryRequestInfo] = ???
    override def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo] = ???
    override def isMethodEntryRequestPending(
      className: String,
      methodName: String
    ): Boolean = ???
    override def isMethodEntryRequestWithArgsPending(
      className: String,
      methodName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def methodEntryRequests: Seq[MethodEntryRequestInfo] = ???
  }

  describe("MethodEntryProfile") {
    describe("#tryGetOrCreateMethodEntryRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MethodEntryEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MethodEntryEventInfoProfile = null
        successMethodEntryProfile.tryGetOrCreateMethodEntryRequest("", "").get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[MethodEntryEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failMethodEntryProfile.tryGetOrCreateMethodEntryRequest("", "").failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateMethodEntryRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MethodEntryEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MethodEntryEventInfoProfile = null
        successMethodEntryProfile
          .getOrCreateMethodEntryRequest("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMethodEntryProfile.getOrCreateMethodEntryRequest("", "")
        }
      }
    }

    describe("#getOrCreateMethodEntryRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MethodEntryEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (MethodEntryEventInfoProfile, Seq[JDIEventDataResult]) = null
        successMethodEntryProfile
          .getOrCreateMethodEntryRequestWithData("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMethodEntryProfile.getOrCreateMethodEntryRequestWithData("", "")
        }
      }
    }
  }
}

