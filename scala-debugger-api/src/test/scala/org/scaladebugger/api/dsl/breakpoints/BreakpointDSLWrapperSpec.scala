package org.scaladebugger.api.dsl.breakpoints

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class BreakpointDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockBreakpointProfile = mock[BreakpointProfile]

  describe("BreakpointDSLWrapper") {
    describe("#onBreakpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.BreakpointDSL

        val fileName = "someFile.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[BreakpointEvent]))

        (mockBreakpointProfile.tryGetOrCreateBreakpointRequest _).expects(
          fileName,
          lineNumber,
          extraArguments
        ).returning(returnValue).once()

        mockBreakpointProfile.onBreakpoint(
          fileName,
          lineNumber,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeBreakpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.BreakpointDSL

        val fileName = "someFile.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[BreakpointEvent])

        (mockBreakpointProfile.getOrCreateBreakpointRequest _).expects(
          fileName,
          lineNumber,
          extraArguments
        ).returning(returnValue).once()

        mockBreakpointProfile.onUnsafeBreakpoint(
          fileName,
          lineNumber,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onBreakpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.BreakpointDSL

        val fileName = "someFile.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(BreakpointEvent, Seq[JDIEventDataResult])]
        ))

        (mockBreakpointProfile.tryGetOrCreateBreakpointRequestWithData _).expects(
          fileName,
          lineNumber,
          extraArguments
        ).returning(returnValue).once()

        mockBreakpointProfile.onBreakpointWithData(
          fileName,
          lineNumber,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeBreakpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.BreakpointDSL

        val fileName = "someFile.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(BreakpointEvent, Seq[JDIEventDataResult])]
        )

        (mockBreakpointProfile.getOrCreateBreakpointRequestWithData _).expects(
          fileName,
          lineNumber,
          extraArguments
        ).returning(returnValue).once()

        mockBreakpointProfile.onUnsafeBreakpointWithData(
          fileName,
          lineNumber,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
