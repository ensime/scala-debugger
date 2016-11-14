package org.scaladebugger.api.dsl.monitors

import com.sun.jdi.event.MonitorContendedEnteredEvent
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.monitors.MonitorContendedEnteredProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class MonitorContendedEnteredDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockMonitorContendedEnteredProfile = mock[MonitorContendedEnteredProfile]

  describe("MonitorContendedEnteredDSLWrapper") {
    describe("#onMonitorContendedEntered") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnteredDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[MonitorContendedEnteredEvent]))

        (mockMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnteredProfile.onMonitorContendedEntered(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorContendedEntered") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnteredDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[MonitorContendedEnteredEvent])

        (mockMonitorContendedEnteredProfile.getOrCreateMonitorContendedEnteredRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnteredProfile.onUnsafeMonitorContendedEntered(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onMonitorContendedEnteredWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnteredDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(MonitorContendedEnteredEvent, Seq[JDIEventDataResult])]
        ))

        (mockMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnteredProfile.onMonitorContendedEnteredWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorContendedEnteredWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnteredDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(MonitorContendedEnteredEvent, Seq[JDIEventDataResult])]
        )

        (mockMonitorContendedEnteredProfile.getOrCreateMonitorContendedEnteredRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnteredProfile.onUnsafeMonitorContendedEnteredWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
