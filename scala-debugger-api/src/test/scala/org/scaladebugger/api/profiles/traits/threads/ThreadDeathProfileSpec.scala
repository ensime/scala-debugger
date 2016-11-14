package org.scaladebugger.api.profiles.traits.threads
import acyclic.file

import com.sun.jdi.event.ThreadDeathEvent
import org.scaladebugger.api.lowlevel.threads.ThreadDeathRequestInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class ThreadDeathProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ThreadDeathProfile#ThreadDeathEventAndData]
  )

  private val successThreadDeathProfile = new Object with ThreadDeathProfile {
    override def tryGetOrCreateThreadDeathRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def isThreadDeathRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeThreadDeathRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ThreadDeathRequestInfo] = ???

    override def removeAllThreadDeathRequests(): Seq[ThreadDeathRequestInfo] = ???

    override def threadDeathRequests: Seq[ThreadDeathRequestInfo] = ???
  }

  private val failThreadDeathProfile = new Object with ThreadDeathProfile {
    override def tryGetOrCreateThreadDeathRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
      Failure(TestThrowable)
    }

    override def isThreadDeathRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeThreadDeathRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ThreadDeathRequestInfo] = ???

    override def removeAllThreadDeathRequests(): Seq[ThreadDeathRequestInfo] = ???

    override def threadDeathRequests: Seq[ThreadDeathRequestInfo] = ???
  }

  describe("ThreadDeathProfile") {
    describe("#tryGetOrCreateThreadDeathRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ThreadDeathEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadDeathEvent = null
        successThreadDeathProfile.tryGetOrCreateThreadDeathRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[ThreadDeathEvent], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failThreadDeathProfile.tryGetOrCreateThreadDeathRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateThreadDeathRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[ThreadDeathEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadDeathEvent = null
        successThreadDeathProfile.getOrCreateThreadDeathRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadDeathProfile.getOrCreateThreadDeathRequest()
        }
      }
    }

    describe("#getOrCreateThreadDeathRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[ThreadDeathEvent], Seq(mock[JDIEventDataResult]))

        var actual: (ThreadDeathEvent, Seq[JDIEventDataResult]) = null
        successThreadDeathProfile
          .getOrCreateThreadDeathRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadDeathProfile.getOrCreateThreadDeathRequestWithData()
        }
      }
    }
  }
}

