package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnteredEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnteredRequest

import scala.util.Success

class MonitorContendedEnteredDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockMonitorContendedEnteredProfile = mock[MonitorContendedEnteredRequest]

  describe("MonitorContendedEnteredDSLWrapper") {
    describe("#onMonitorContendedEntered") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnteredDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[MonitorContendedEnteredEventInfo]))

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
        val returnValue = Pipeline.newPipeline(classOf[MonitorContendedEnteredEventInfo])

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
          classOf[(MonitorContendedEnteredEventInfo, Seq[JDIEventDataResult])]
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
          classOf[(MonitorContendedEnteredEventInfo, Seq[JDIEventDataResult])]
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
