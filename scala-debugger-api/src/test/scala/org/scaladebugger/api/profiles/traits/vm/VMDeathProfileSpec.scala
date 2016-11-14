package org.scaladebugger.api.profiles.traits.vm
import acyclic.file

import com.sun.jdi.event.VMDeathEvent
import org.scaladebugger.api.lowlevel.vm.VMDeathRequestInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class VMDeathProfileSpec extends test.ParallelMockFunSpec
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[VMDeathProfile#VMDeathEventAndData]
  )

  private val successVMDeathProfile = new Object with VMDeathProfile {
    override def tryGetOrCreateVMDeathRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMDeathEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def isVMDeathRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeVMDeathRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[VMDeathRequestInfo] = ???

    override def removeAllVMDeathRequests(): Seq[VMDeathRequestInfo] = ???

    override def vmDeathRequests: Seq[VMDeathRequestInfo] = ???
  }

  private val failVMDeathProfile = new Object with VMDeathProfile {
    override def tryGetOrCreateVMDeathRequestWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[VMDeathEventAndData]] = {
      Failure(TestThrowable)
    }

    override def isVMDeathRequestWithArgsPending(
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def removeVMDeathRequestWithArgs(
      extraArguments: JDIArgument*
    ): Option[VMDeathRequestInfo] = ???

    override def removeAllVMDeathRequests(): Seq[VMDeathRequestInfo] = ???

    override def vmDeathRequests: Seq[VMDeathRequestInfo] = ???
  }

  describe("VMDeathProfile") {
    describe("#tryGetOrCreateVMDeathRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[VMDeathEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMDeathEvent = null
        successVMDeathProfile.tryGetOrCreateVMDeathRequest().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[VMDeathEvent], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failVMDeathProfile.tryGetOrCreateVMDeathRequest().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateVMDeathRequest") {
      it("should return a pipeline of events if successful") {
        val expected = mock[VMDeathEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: VMDeathEvent = null
        successVMDeathProfile.getOrCreateVMDeathRequest().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMDeathProfile.getOrCreateVMDeathRequest()
        }
      }
    }

    describe("#getOrCreateVMDeathRequestWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[VMDeathEvent], Seq(mock[JDIEventDataResult]))

        var actual: (VMDeathEvent, Seq[JDIEventDataResult]) = null
        successVMDeathProfile
          .getOrCreateVMDeathRequestWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failVMDeathProfile.getOrCreateVMDeathRequestWithData()
        }
      }
    }
  }
}

