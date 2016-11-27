package org.scaladebugger.api.dsl.steps

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile
import org.scaladebugger.api.profiles.traits.steps.StepProfile

import scala.util.Success

class StepDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockStepProfile = mock[StepProfile]

  describe("StepDSLWrapper") {
    describe("#onStep") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.StepDSL

        val threadInfoProfile = mock[ThreadInfoProfile]
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[StepEventInfoProfile]))

        (mockStepProfile.tryCreateStepListener _).expects(
          threadInfoProfile,
          extraArguments
        ).returning(returnValue).once()

        mockStepProfile.onStep(
          threadInfoProfile,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeStep") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.StepDSL

        val threadInfoProfile = mock[ThreadInfoProfile]
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[StepEventInfoProfile])

        (mockStepProfile.createStepListener _).expects(
          threadInfoProfile,
          extraArguments
        ).returning(returnValue).once()

        mockStepProfile.onUnsafeStep(
          threadInfoProfile,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onStepWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.StepDSL

        val threadInfoProfile = mock[ThreadInfoProfile]
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(StepEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockStepProfile.tryCreateStepListenerWithData _).expects(
          threadInfoProfile,
          extraArguments
        ).returning(returnValue).once()

        mockStepProfile.onStepWithData(
          threadInfoProfile,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeStepWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.StepDSL

        val threadInfoProfile = mock[ThreadInfoProfile]
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(StepEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockStepProfile.createStepListenerWithData _).expects(
          threadInfoProfile,
          extraArguments
        ).returning(returnValue).once()

        mockStepProfile.onUnsafeStepWithData(
          threadInfoProfile,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
