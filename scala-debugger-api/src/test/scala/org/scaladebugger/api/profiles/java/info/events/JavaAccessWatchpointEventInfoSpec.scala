package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaAccessWatchpointEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockAccessWatchpointEvent = mock[AccessWatchpointEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockContainerObjectReference = Left(mock[ObjectReference])
  private val mockField = mock[Field]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]
  private val testOffsetIndex = -1

  private val javaAccessWatchpointEventInfoProfile = new JavaAccessWatchpointEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    accessWatchpointEvent = mockAccessWatchpointEvent,
    jdiArguments = mockJdiArguments
  )(
    _container = mockContainerObjectReference,
    _field = mockField,
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("JavaAccessWatchpointEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[AccessWatchpointEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newAccessWatchpointEventInfo(
          _: ScalaVirtualMachine,
          _: AccessWatchpointEvent,
          _: Seq[JDIArgument]
        )(
          _: Either[ObjectReference, ReferenceType],
          _: Field,
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockAccessWatchpointEvent,
          mockJdiArguments,
          *, *, *, *, *, *
        ).returning(expected).once()

        val actual = javaAccessWatchpointEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaAccessWatchpointEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockAccessWatchpointEvent

        val actual = javaAccessWatchpointEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }
  }
}
