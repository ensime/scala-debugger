package org.scaladebugger.api.dsl.classes

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.requests.classes.ClassPrepareProfile
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfoProfile

import scala.util.Success

class ClassPrepareDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockClassPrepareProfile = mock[ClassPrepareProfile]

  describe("ClassPrepareDSLWrapper") {
    describe("#onClassPrepare") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassPrepareDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ClassPrepareEventInfoProfile]))

        (mockClassPrepareProfile.tryGetOrCreateClassPrepareRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassPrepareProfile.onClassPrepare(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeClassPrepare") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassPrepareDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ClassPrepareEventInfoProfile])

        (mockClassPrepareProfile.getOrCreateClassPrepareRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassPrepareProfile.onUnsafeClassPrepare(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onClassPrepareWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassPrepareDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ClassPrepareEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockClassPrepareProfile.tryGetOrCreateClassPrepareRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassPrepareProfile.onClassPrepareWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeClassPrepareWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassPrepareDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ClassPrepareEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockClassPrepareProfile.getOrCreateClassPrepareRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassPrepareProfile.onUnsafeClassPrepareWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
