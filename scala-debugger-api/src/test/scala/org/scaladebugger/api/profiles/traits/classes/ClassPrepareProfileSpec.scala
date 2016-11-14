package org.scaladebugger.api.profiles.traits.classes
import acyclic.file

import com.sun.jdi.event.ClassPrepareEvent
import org.scaladebugger.api.lowlevel.classes.ClassPrepareRequestInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class ClassPrepareProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ClassPrepareProfile#ClassPrepareEventAndData]
  )

  private val successClassPrepareProfile = new Object with ClassPrepareProfile {
    override def tryGetOrCreateClassPrepareRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ClassPrepareEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeClassPrepareRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ClassPrepareRequestInfo] = ???

    override def removeAllClassPrepareRequests(): Seq[ClassPrepareRequestInfo] = ???

    override def isClassPrepareRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def classPrepareRequests: Seq[ClassPrepareRequestInfo] = ???
  }

  private val failClassPrepareProfile = new Object with ClassPrepareProfile {
    override def tryGetOrCreateClassPrepareRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ClassPrepareEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeClassPrepareRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[ClassPrepareRequestInfo] = ???

    override def removeAllClassPrepareRequests(): Seq[ClassPrepareRequestInfo] = ???

    override def isClassPrepareRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def classPrepareRequests: Seq[ClassPrepareRequestInfo] = ???
  }

  describe("ClassPrepareProfile") {
    describe("#tryGetOrCreateClassPrepareRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ClassPrepareEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ClassPrepareEvent = null
        successClassPrepareProfile.tryGetOrCreateClassPrepareRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[ClassPrepareEvent], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failClassPrepareProfile.tryGetOrCreateClassPrepareRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateClassPrepareRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[ClassPrepareEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ClassPrepareEvent = null
        successClassPrepareProfile.getOrCreateClassPrepareRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failClassPrepareProfile.getOrCreateClassPrepareRequest()
        }
      }
    }

    describe("#getOrCreateClassPrepareRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[ClassPrepareEvent], Seq(mock[JDIEventDataResult]))

        var actual: (ClassPrepareEvent, Seq[JDIEventDataResult]) = null
        successClassPrepareProfile
          .getOrCreateClassPrepareRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failClassPrepareProfile.getOrCreateClassPrepareRequestWithData()
        }
      }
    }
  }
}

