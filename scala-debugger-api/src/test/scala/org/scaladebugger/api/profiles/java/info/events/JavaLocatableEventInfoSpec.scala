package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi.event._
import com.sun.jdi.{Location, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, LocationInfo, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaLocatableEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockLocatableEvent = mock[LocatableEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val javaLocatableEventInfoProfile = new JavaLocatableEventInfo(
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

  describe("JavaLocatableEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[LocatableEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newLocatableEventInfo(
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

        val actual = javaLocatableEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaLocatableEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocatableEvent

        val actual = javaLocatableEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#thread") {
      it("should return a new instance of the thread info profile") {
        val expected = mock[ThreadInfo]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newThreadInfo(
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

        val actual = javaLocatableEventInfoProfile.thread

        actual should be (expected)
      }
    }

    describe("#location") {
      it("should return a new instance of the location info profile") {
        val expected = mock[LocationInfo]

        (mockInfoProducer.newLocationInfo _)
          .expects(mockScalaVirtualMachine, mockLocation)
          .returning(expected).once()

        val actual = javaLocatableEventInfoProfile.location

        actual should be (expected)
      }
    }

    describe("#toString") {
      it("should return the string representation of the JDI event object") {
        val expected = mockLocatableEvent.toString // NOTE: Cannot mock toString method

        val actual = javaLocatableEventInfoProfile.toString

        actual should be (expected)
      }
    }
  }
}
