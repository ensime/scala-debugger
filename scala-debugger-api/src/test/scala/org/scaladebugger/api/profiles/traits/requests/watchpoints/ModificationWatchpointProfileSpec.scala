package org.scaladebugger.api.profiles.traits.requests.watchpoints
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.watchpoints.ModificationWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ModificationWatchpointEventInfoProfile

import scala.util.{Failure, Success, Try}

class ModificationWatchpointProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ModificationWatchpointProfile#ModificationWatchpointEventAndData]
  )

  private val successModificationWatchpointProfile = new Object with ModificationWatchpointProfile {
    override def tryGetOrCreateModificationWatchpointRequestWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeModificationWatchpointRequests(
      className: String,
      fieldName: String
    ): Seq[ModificationWatchpointRequestInfo] = ???
    override def removeModificationWatchpointRequestWithArgs(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Option[ModificationWatchpointRequestInfo] = ???
    override def removeAllModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo] = ???
    override def isModificationWatchpointRequestPending(
      className: String,
      fieldName: String
    ): Boolean = ???
    override def isModificationWatchpointRequestWithArgsPending(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def modificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo] = ???
  }

  private val failModificationWatchpointProfile = new Object with ModificationWatchpointProfile {
    override def tryGetOrCreateModificationWatchpointRequestWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Failure(TestThrowable)
    }

    override def removeModificationWatchpointRequests(
      className: String,
      fieldName: String
    ): Seq[ModificationWatchpointRequestInfo] = ???
    override def removeModificationWatchpointRequestWithArgs(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Option[ModificationWatchpointRequestInfo] = ???
    override def removeAllModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo] = ???
    override def isModificationWatchpointRequestPending(
      className: String,
      fieldName: String
    ): Boolean = ???
    override def isModificationWatchpointRequestWithArgsPending(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def modificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo] = ???
  }

  describe("ModificationWatchpointProfile") {
    describe("#tryGetOrCreateModificationWatchpointRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEventInfoProfile = null
        successModificationWatchpointProfile
          .tryGetOrCreateModificationWatchpointRequest("", "")
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
        failModificationWatchpointProfile
          .tryGetOrCreateModificationWatchpointRequest("", "")
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateModificationWatchpointRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEventInfoProfile = null
        successModificationWatchpointProfile
          .getOrCreateModificationWatchpointRequest("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile.getOrCreateModificationWatchpointRequest("", "")
        }
      }
    }

    describe("#getOrCreateModificationWatchpointRequestWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[ModificationWatchpointEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (ModificationWatchpointEventInfoProfile, Seq[JDIEventDataResult]) = null
        successModificationWatchpointProfile
          .getOrCreateModificationWatchpointRequestWithData("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile
            .getOrCreateModificationWatchpointRequestWithData("", "")
        }
      }
    }
  }
}

