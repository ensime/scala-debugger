package org.scaladebugger.api.profiles.pure.vm
import acyclic.file

import com.sun.jdi.event.{EventQueue, Event, VMDisconnectEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.api.lowlevel.events.EventType.VMDisconnectEventType
import test.JDIMockHelpers

class PureVMDisconnectProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]

  private val pureVMDisconnectProfile = new Object with PureVMDisconnectProfile {
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureVMDisconnectProfile") {
    describe("#tryGetOrCreateVMDisconnectRequestWithData") {
      it("should create a stream of events with data for disconnections") {
        val expected = (mock[VMDisconnectEvent], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        (mockEventManager.addEventDataStream _).expects(
          VMDisconnectEventType, arguments
        ).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
            .map(t => (expected._1, expected._2))
        ).once()

        var actual: (VMDisconnectEvent, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMDisconnectProfile.tryGetOrCreateVMDisconnectRequestWithData(arguments: _*)
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
