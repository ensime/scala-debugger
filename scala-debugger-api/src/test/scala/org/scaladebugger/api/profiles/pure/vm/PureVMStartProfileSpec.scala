package org.scaladebugger.api.profiles.pure.vm
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventType.VMStartEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.JDIMockHelpers

class PureVMStartProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureVMStartProfile = new Object with PureVMStartProfile {
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureVMStartProfile") {
    describe("#tryGetOrCreateVMStartRequestWithData") {
      it("should create a stream of events with data for when a vm starts") {
        val expected = (mock[VMStartEventInfoProfile], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        (mockEventManager.addEventDataStream _).expects(
          VMStartEventType, arguments
        ).returning(lowlevelPipeline).once()

        var actual: (VMStartEventInfoProfile, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMStartProfile.tryGetOrCreateVMStartRequestWithData(arguments: _*)
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
