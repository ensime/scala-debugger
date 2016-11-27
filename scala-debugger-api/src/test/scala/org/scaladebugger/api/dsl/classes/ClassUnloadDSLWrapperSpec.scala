package org.scaladebugger.api.dsl.classes

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.classes.ClassUnloadProfile
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile

import scala.util.Success

class ClassUnloadDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockClassUnloadProfile = mock[ClassUnloadProfile]

  describe("ClassUnloadDSLWrapper") {
    describe("#onClassUnload") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassUnloadDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ClassUnloadEventInfoProfile]))

        (mockClassUnloadProfile.tryGetOrCreateClassUnloadRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassUnloadProfile.onClassUnload(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeClassUnload") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassUnloadDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ClassUnloadEventInfoProfile])

        (mockClassUnloadProfile.getOrCreateClassUnloadRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassUnloadProfile.onUnsafeClassUnload(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onClassUnloadWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassUnloadDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ClassUnloadEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockClassUnloadProfile.tryGetOrCreateClassUnloadRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassUnloadProfile.onClassUnloadWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeClassUnloadWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ClassUnloadDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ClassUnloadEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockClassUnloadProfile.getOrCreateClassUnloadRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockClassUnloadProfile.onUnsafeClassUnloadWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
