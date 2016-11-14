package org.scaladebugger.api.dsl.monitors

import com.sun.jdi.event.MonitorWaitEvent
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.monitors.MonitorWaitProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class MonitorWaitDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockMonitorWaitProfile = mock[MonitorWaitProfile]

  describe("MonitorWaitDSLWrapper") {
    describe("#onMonitorWait") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorWaitDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[MonitorWaitEvent]))

        (mockMonitorWaitProfile.tryGetOrCreateMonitorWaitRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorWaitProfile.onMonitorWait(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorWait") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorWaitDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[MonitorWaitEvent])

        (mockMonitorWaitProfile.getOrCreateMonitorWaitRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorWaitProfile.onUnsafeMonitorWait(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onMonitorWaitWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorWaitDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(MonitorWaitEvent, Seq[JDIEventDataResult])]
        ))

        (mockMonitorWaitProfile.tryGetOrCreateMonitorWaitRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorWaitProfile.onMonitorWaitWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorWaitWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorWaitDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(MonitorWaitEvent, Seq[JDIEventDataResult])]
        )

        (mockMonitorWaitProfile.getOrCreateMonitorWaitRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorWaitProfile.onUnsafeMonitorWaitWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
