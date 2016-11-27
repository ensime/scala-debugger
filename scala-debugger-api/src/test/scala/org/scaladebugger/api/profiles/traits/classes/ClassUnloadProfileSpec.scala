package org.scaladebugger.api.profiles.traits.classes
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassUnloadRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile

import scala.util.{Failure, Success, Try}

class ClassUnloadProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ClassUnloadProfile#ClassUnloadEventAndData]
  )

  private val successClassUnloadProfile = new Object with ClassUnloadProfile {
    override def tryGetOrCreateClassUnloadRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ClassUnloadEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeClassUnloadRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ClassUnloadRequestInfo] = ???

    override def removeAllClassUnloadRequests(): Seq[ClassUnloadRequestInfo] = ???

    override def isClassUnloadRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def classUnloadRequests: Seq[ClassUnloadRequestInfo] = ???
  }

  private val failClassUnloadProfile = new Object with ClassUnloadProfile {
    override def tryGetOrCreateClassUnloadRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ClassUnloadEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeClassUnloadRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ClassUnloadRequestInfo] = ???

    override def removeAllClassUnloadRequests(): Seq[ClassUnloadRequestInfo] = ???

    override def isClassUnloadRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def classUnloadRequests: Seq[ClassUnloadRequestInfo] = ???
  }

  describe("ClassUnloadProfile") {
    describe("#tryGetOrCreateClassUnloadRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ClassUnloadEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ClassUnloadEventInfoProfile = null
        successClassUnloadProfile.tryGetOrCreateClassUnloadRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[ClassUnloadEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failClassUnloadProfile.tryGetOrCreateClassUnloadRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateClassUnloadRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[ClassUnloadEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ClassUnloadEventInfoProfile = null
        successClassUnloadProfile.getOrCreateClassUnloadRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failClassUnloadProfile.getOrCreateClassUnloadRequest()
        }
      }
    }

    describe("#getOrCreateClassUnloadRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[ClassUnloadEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (ClassUnloadEventInfoProfile, Seq[JDIEventDataResult]) = null
        successClassUnloadProfile
          .getOrCreateClassUnloadRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failClassUnloadProfile.getOrCreateClassUnloadRequestWithData()
        }
      }
    }
  }
}

