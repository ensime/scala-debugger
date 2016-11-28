package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.{Location, ReferenceType, ThreadReference, VirtualMachine}
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, LocationInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureLocatableEventInfoProfileSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockLocatableEvent = mock[LocatableEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureLocatableEventInfoProfile = new PureLocatableEventInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    locatableEvent = mockLocatableEvent,
    jdiArguments = mockJdiArguments
  )(
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureLocatableEventInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[LocatableEventInfoProfile]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducerProfile]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newLocatableEventInfoProfile(
          _: ScalaVirtualMachine,
          _: LocatableEvent,
          _: Seq[JDIArgument]
        )(
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockLocatableEvent,
          mockJdiArguments,
          *, *, *, *
        ).returning(expected).once()

        val actual = pureLocatableEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureLocatableEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocatableEvent

        val actual = pureLocatableEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#thread") {
      it("should return a new instance of the thread info profile") {
        val expected = mock[ThreadInfoProfile]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newThreadInfoProfile(
          _: ScalaVirtualMachine,
          _: ThreadReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockThreadReference,
          *, *
        ).returning(expected).once()

        val actual = pureLocatableEventInfoProfile.thread

        actual should be (expected)
      }
    }

    describe("#location") {
      it("should return a new instance of the location info profile") {
        val expected = mock[LocationInfoProfile]

        (mockInfoProducer.newLocationInfoProfile _)
          .expects(mockScalaVirtualMachine, mockLocation)
          .returning(expected).once()

        val actual = pureLocatableEventInfoProfile.location

        actual should be (expected)
      }
    }

    describe("#toString") {
      it("should return the string representation of the JDI event object") {
        val expected = mockLocatableEvent.toString // NOTE: Cannot mock toString method

        val actual = pureLocatableEventInfoProfile.toString

        actual should be (expected)
      }
    }
  }
}
