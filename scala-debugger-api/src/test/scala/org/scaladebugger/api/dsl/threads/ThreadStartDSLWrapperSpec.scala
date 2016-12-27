package org.scaladebugger.api.dsl.threads

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadStartEventInfo
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadStartRequest
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.Success

class ThreadStartDSLWrapperSpec extends ParallelMockFunSpec
{
  private val mockThreadStartProfile = mock[ThreadStartRequest]

  describe("ThreadStartDSLWrapper") {
    describe("#onThreadStart") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ThreadStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ThreadStartEventInfo]))

        (mockThreadStartProfile.tryGetOrCreateThreadStartRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockThreadStartProfile.onThreadStart(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeThreadStart") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ThreadStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ThreadStartEventInfo])

        (mockThreadStartProfile.getOrCreateThreadStartRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockThreadStartProfile.onUnsafeThreadStart(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onThreadStartWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ThreadStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ThreadStartEventInfo, Seq[JDIEventDataResult])]
        ))

        (mockThreadStartProfile.tryGetOrCreateThreadStartRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockThreadStartProfile.onThreadStartWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeThreadStartWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ThreadStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ThreadStartEventInfo, Seq[JDIEventDataResult])]
        )

        (mockThreadStartProfile.getOrCreateThreadStartRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockThreadStartProfile.onUnsafeThreadStartWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
