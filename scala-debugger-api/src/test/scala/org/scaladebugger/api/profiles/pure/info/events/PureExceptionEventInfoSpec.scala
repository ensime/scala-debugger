package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, LocationInfo, ObjectInfo}
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class PureExceptionEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockExceptionEvent = mock[ExceptionEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockCatchLocation = mock[Location]
  private val mockExceptionReference = mock[ObjectReference]
  private val mockExceptionReferenceType = mock[ReferenceType]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureExceptionEventInfoProfile = new PureExceptionEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    exceptionEvent = mockExceptionEvent,
    jdiArguments = mockJdiArguments
  )(
    _catchLocation = Some(mockCatchLocation),
    _exception = mockExceptionReference,
    _exceptionReferenceType = mockExceptionReferenceType,
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureExceptionEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ExceptionEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newExceptionEventInfo(
          _: ScalaVirtualMachine,
          _: ExceptionEvent,
          _: Seq[JDIArgument]
        )(
          _: Option[Location],
          _: ObjectReference,
          _: ReferenceType,
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockExceptionEvent,
          mockJdiArguments,
          *, *, *, *, *, *, *
        ).returning(expected).once()

        val actual = pureExceptionEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureExceptionEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockExceptionEvent

        val actual = pureExceptionEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#catchLocation") {
      it("should return Some new location profile if there is a catch location") {
        val expected = Some(mock[LocationInfo])

        // Verify catch location is fed into location info producer
        (mockInfoProducer.newLocationInfo _)
          .expects(mockScalaVirtualMachine, mockCatchLocation)
          .returning(expected.get).once()

        val actual = pureExceptionEventInfoProfile.catchLocation

        actual should be (expected)
      }

      it("should return None if there is no catch location") {
        val expected = None

        // Indicate that there is no catch location
        val pureExceptionEventInfoProfile = new PureExceptionEventInfo(
          scalaVirtualMachine = mockScalaVirtualMachine,
          infoProducer = mockInfoProducer,
          exceptionEvent = mockExceptionEvent,
          jdiArguments = mockJdiArguments
        )(
          _catchLocation = None,
          _exception = mockExceptionReference,
          _exceptionReferenceType = mockExceptionReferenceType,
          _virtualMachine = mockVirtualMachine,
          _thread = mockThreadReference,
          _threadReferenceType = mockThreadReferenceType,
          _location = mockLocation
        )

        val actual = pureExceptionEventInfoProfile.catchLocation

        actual should be (expected)
      }
    }

    describe("#exception") {
      it("should return a new object info profile for the exception object") {
        val expected = mock[ObjectInfo]

        // Verify catch location is fed into location info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newObjectInfo(
          _: ScalaVirtualMachine,
          _: ObjectReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockExceptionReference,
          *, *
        ).returning(expected).once()

        val actual = pureExceptionEventInfoProfile.exception

        actual should be (expected)
      }
    }
  }
}
