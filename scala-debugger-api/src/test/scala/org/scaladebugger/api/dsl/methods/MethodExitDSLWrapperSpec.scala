package org.scaladebugger.api.dsl.methods

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfo
import org.scaladebugger.api.profiles.traits.requests.methods.MethodExitRequest

import scala.util.Success

class MethodExitDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockMethodExitProfile = mock[MethodExitRequest]

  describe("MethodExitDSLWrapper") {
    describe("#onMethodExit") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MethodExitDSL

        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[MethodExitEventInfo]))

        (mockMethodExitProfile.tryGetOrCreateMethodExitRequest _).expects(
          className,
          methodName,
          extraArguments
        ).returning(returnValue).once()

        mockMethodExitProfile.onMethodExit(
          className,
          methodName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMethodExit") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MethodExitDSL

        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[MethodExitEventInfo])

        (mockMethodExitProfile.getOrCreateMethodExitRequest _).expects(
          className,
          methodName,
          extraArguments
        ).returning(returnValue).once()

        mockMethodExitProfile.onUnsafeMethodExit(
          className,
          methodName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onMethodExitWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MethodExitDSL

        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(MethodExitEventInfo, Seq[JDIEventDataResult])]
        ))

        (mockMethodExitProfile.tryGetOrCreateMethodExitRequestWithData _).expects(
          className,
          methodName,
          extraArguments
        ).returning(returnValue).once()

        mockMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMethodExitWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MethodExitDSL

        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(MethodExitEventInfo, Seq[JDIEventDataResult])]
        )

        (mockMethodExitProfile.getOrCreateMethodExitRequestWithData _).expects(
          className,
          methodName,
          extraArguments
        ).returning(returnValue).once()

        mockMethodExitProfile.onUnsafeMethodExitWithData(
          className,
          methodName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
