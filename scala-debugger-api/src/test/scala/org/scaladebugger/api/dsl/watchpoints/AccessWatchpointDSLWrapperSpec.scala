package org.scaladebugger.api.dsl.watchpoints

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.watchpoints.AccessWatchpointProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class AccessWatchpointDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockAccessWatchpointProfile = mock[AccessWatchpointProfile]

  describe("AccessWatchpointDSLWrapper") {
    describe("#onAccessWatchpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.AccessWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[AccessWatchpointEvent]))

        (mockAccessWatchpointProfile.tryGetOrCreateAccessWatchpointRequest _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockAccessWatchpointProfile.onAccessWatchpoint(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeAccessWatchpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.AccessWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[AccessWatchpointEvent])

        (mockAccessWatchpointProfile.getOrCreateAccessWatchpointRequest _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockAccessWatchpointProfile.onUnsafeAccessWatchpoint(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onAccessWatchpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.AccessWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(AccessWatchpointEvent, Seq[JDIEventDataResult])]
        ))

        (mockAccessWatchpointProfile.tryGetOrCreateAccessWatchpointRequestWithData _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeAccessWatchpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.AccessWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(AccessWatchpointEvent, Seq[JDIEventDataResult])]
        )

        (mockAccessWatchpointProfile.getOrCreateAccessWatchpointRequestWithData _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockAccessWatchpointProfile.onUnsafeAccessWatchpointWithData(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
