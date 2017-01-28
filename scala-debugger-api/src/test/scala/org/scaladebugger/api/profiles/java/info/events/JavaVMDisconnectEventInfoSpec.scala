package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi.event._
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaVMDisconnectEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockVMDisconnectEvent = mock[VMDisconnectEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val javaVMDisconnectEventInfoProfile = new JavaVMDisconnectEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    vmDisconnectEvent = mockVMDisconnectEvent,
    jdiArguments = mockJdiArguments
  )

  describe("JavaVMDisconnectEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[VMDisconnectEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newVMDisconnectEventInfo _).expects(
          mockScalaVirtualMachine,
          mockVMDisconnectEvent,
          mockJdiArguments
        ).returning(expected).once()

        val actual = javaVMDisconnectEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaVMDisconnectEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockVMDisconnectEvent

        val actual = javaVMDisconnectEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }
  }
}
