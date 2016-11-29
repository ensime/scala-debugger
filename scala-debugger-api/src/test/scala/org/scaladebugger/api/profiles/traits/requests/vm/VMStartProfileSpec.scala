package org.scaladebugger.api.profiles.traits.requests.vm
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfoProfile

import scala.util.{Failure, Success, Try}

class VMStartProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[VMStartProfile#VMStartEventAndData]
  )

  private val successVMStartProfile = new Object with VMStartProfile {
    override def tryGetOrCreateVMStartRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMStartEventAndData]] = {
      Success(TestPipelineWithData)
    }
  }

  private val failVMStartProfile = new Object with VMStartProfile {
    override def tryGetOrCreateVMStartRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMStartEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("VMStartProfile") {
    describe("#tryGetOrCreateVMStartRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[VMStartEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMStartEventInfoProfile = null
        successVMStartProfile.tryGetOrCreateVMStartRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[VMStartEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failVMStartProfile.tryGetOrCreateVMStartRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateVMStartRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[VMStartEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMStartEventInfoProfile = null
        successVMStartProfile.getOrCreateVMStartRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMStartProfile.getOrCreateVMStartRequest()
        }
      }
    }

    describe("#getOrCreateVMStartRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[VMStartEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (VMStartEventInfoProfile, Seq[JDIEventDataResult]) = null
        successVMStartProfile
          .getOrCreateVMStartRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMStartProfile.getOrCreateVMStartRequestWithData()
        }
      }
    }
  }
}

