package org.scaladebugger.api.profiles.pure.requests.vm
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventType.VMDisconnectEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.JDIMockHelpers

class PureVMDisconnectProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureVMDisconnectProfile = new Object with PureVMDisconnectProfile {
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureVMDisconnectProfile") {
    describe("#tryGetOrCreateVMDisconnectRequestWithData") {
      it("should create a stream of events with data for disconnections") {
        val expected = (mock[VMDisconnectEventInfoProfile], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        (mockEventManager.addEventDataStream _).expects(
          VMDisconnectEventType, arguments
        ).returning(lowlevelPipeline).once()

        var actual: (VMDisconnectEventInfoProfile, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMDisconnectProfile.tryGetOrCreateVMDisconnectRequestWithData(arguments: _*)
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
