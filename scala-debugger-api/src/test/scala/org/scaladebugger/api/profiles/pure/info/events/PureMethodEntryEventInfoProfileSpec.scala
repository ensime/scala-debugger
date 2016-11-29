package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, MethodInfoProfile}
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureMethodEntryEventInfoProfileSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockMethodEntryEvent = mock[MethodEntryEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockMethod = mock[Method]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureMethodEntryEventInfoProfile = new PureMethodEntryEventInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    methodEntryEvent = mockMethodEntryEvent,
    jdiArguments = mockJdiArguments
  )(
    _method = mockMethod,
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureMethodEntryEventInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[MethodEntryEventInfoProfile]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducerProfile]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newMethodEntryEventInfoProfile(
          _: ScalaVirtualMachine,
          _: MethodEntryEvent,
          _: Seq[JDIArgument]
        )(
          _: Method,
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockMethodEntryEvent,
          mockJdiArguments,
          *, *, *, *, *
        ).returning(expected).once()

        val actual = pureMethodEntryEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureMethodEntryEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockMethodEntryEvent

        val actual = pureMethodEntryEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#method") {
      it("should return a new method info profile for the jdi method") {
        val expected = mock[MethodInfoProfile]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newMethodInfoProfile _)
          .expects(mockScalaVirtualMachine, mockMethod)
          .returning(expected).once()

        val actual = pureMethodEntryEventInfoProfile.method

        actual should be (expected)
      }
    }
  }
}
