package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import com.sun.jdi.{Location, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureStepEventInfoSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockStepEvent = mock[StepEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureStepEventInfoProfile = new PureStepEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    stepEvent = mockStepEvent,
    jdiArguments = mockJdiArguments
  )(
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureStepEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[StepEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newStepEventInfoProfile(
          _: ScalaVirtualMachine,
          _: StepEvent,
          _: Seq[JDIArgument]
        )(
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          mockJdiArguments,
          *, *, *, *
        ).returning(expected).once()

        val actual = pureStepEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureStepEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockStepEvent

        val actual = pureStepEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }
  }
}
