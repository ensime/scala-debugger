package org.scaladebugger.api.profiles.pure.vm
import acyclic.file

import com.sun.jdi.event.{EventQueue, Event, VMStartEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType.VMStartEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

class PureVMStartProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]

  private val pureVMStartProfile = new Object with PureVMStartProfile {
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureVMStartProfile") {
    describe("#tryGetOrCreateVMStartRequestWithData") {
      it("should create a stream of events with data for when a vm starts") {
        val expected = (mock[VMStartEvent], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        (mockEventManager.addEventDataStream _).expects(
          VMStartEventType, arguments
        ).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
            .map(t => (expected._1, expected._2))
        ).once()

        var actual: (VMStartEvent, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMStartProfile.tryGetOrCreateVMStartRequestWithData(arguments: _*)
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
