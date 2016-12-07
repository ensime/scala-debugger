package org.scaladebugger.api.profiles.traits.requests.threads
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.threads.ThreadStartRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadStartEventInfo

import scala.util.{Failure, Success, Try}

class ThreadStartRequestSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ThreadStartRequest#ThreadStartEventAndData]
  )

  private val successThreadStartProfile = new Object with ThreadStartRequest {
    override def tryGetOrCreateThreadStartRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def isThreadStartRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeThreadStartRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ThreadStartRequestInfo] = ???

    override def removeAllThreadStartRequests(): Seq[ThreadStartRequestInfo] = ???

    override def threadStartRequests: Seq[ThreadStartRequestInfo] = ???
  }

  private val failThreadStartProfile = new Object with ThreadStartRequest {
    override def tryGetOrCreateThreadStartRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
      Failure(TestThrowable)
    }

    override def isThreadStartRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeThreadStartRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ThreadStartRequestInfo] = ???

    override def removeAllThreadStartRequests(): Seq[ThreadStartRequestInfo] = ???

    override def threadStartRequests: Seq[ThreadStartRequestInfo] = ???
  }

  describe("ThreadStartRequest") {
    describe("#tryGetOrCreateThreadStartRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ThreadStartEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadStartEventInfo = null
        successThreadStartProfile.tryGetOrCreateThreadStartRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[ThreadStartEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failThreadStartProfile.tryGetOrCreateThreadStartRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateThreadStartRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[ThreadStartEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadStartEventInfo = null
        successThreadStartProfile.getOrCreateThreadStartRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadStartProfile.getOrCreateThreadStartRequest()
        }
      }
    }

    describe("#getOrCreateThreadStartRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[ThreadStartEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (ThreadStartEventInfo, Seq[JDIEventDataResult]) = null
        successThreadStartProfile
          .getOrCreateThreadStartRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadStartProfile.getOrCreateThreadStartRequestWithData()
        }
      }
    }
  }
}

