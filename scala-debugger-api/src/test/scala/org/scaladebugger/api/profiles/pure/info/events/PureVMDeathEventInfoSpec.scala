package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureVMDeathEventInfoSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockVMDeathEvent = mock[VMDeathEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val pureVMDeathEventInfoProfile = new PureVMDeathEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    vmDeathEvent = mockVMDeathEvent,
    jdiArguments = mockJdiArguments
  )

  describe("PureVMDeathEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[VMDeathEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newVMDeathEventInfoProfile _).expects(
          mockScalaVirtualMachine,
          mockVMDeathEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = pureVMDeathEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureVMDeathEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockVMDeathEvent

        val actual = pureVMDeathEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }
  }
}
