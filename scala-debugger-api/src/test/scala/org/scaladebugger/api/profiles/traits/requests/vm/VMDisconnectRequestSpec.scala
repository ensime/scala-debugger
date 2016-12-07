package org.scaladebugger.api.profiles.traits.requests.vm
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfo

import scala.util.{Failure, Success, Try}

class VMDisconnectRequestSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[VMDisconnectRequest#VMDisconnectEventAndData]
  )

  private val successVMDisconnectProfile = new Object with VMDisconnectRequest {
    override def tryGetOrCreateVMDisconnectRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMDisconnectEventAndData]] = {
      Success(TestPipelineWithData)
    }
  }

  private val failVMDisconnectProfile = new Object with VMDisconnectRequest {
    override def tryGetOrCreateVMDisconnectRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMDisconnectEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("VMDisconnectRequest") {
    describe("#tryGetOrCreateVMDisconnectRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[VMDisconnectEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMDisconnectEventInfo = null
        successVMDisconnectProfile.tryGetOrCreateVMDisconnectRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[VMDisconnectEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failVMDisconnectProfile.tryGetOrCreateVMDisconnectRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateVMDisconnectRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[VMDisconnectEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMDisconnectEventInfo = null
        successVMDisconnectProfile.getOrCreateVMDisconnectRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMDisconnectProfile.getOrCreateVMDisconnectRequest()
        }
      }
    }

    describe("#getOrCreateVMDisconnectRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[VMDisconnectEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (VMDisconnectEventInfo, Seq[JDIEventDataResult]) = null
        successVMDisconnectProfile
          .getOrCreateVMDisconnectRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMDisconnectProfile.getOrCreateVMDisconnectRequestWithData()
        }
      }
    }
  }
}

