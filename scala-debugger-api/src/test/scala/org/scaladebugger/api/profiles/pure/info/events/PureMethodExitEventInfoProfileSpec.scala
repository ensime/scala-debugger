package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, MethodInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureMethodExitEventInfoProfileSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockMethodExitEvent = mock[MethodExitEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockMethod = mock[Method]
  private val mockReturnValue = mock[Value]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureMethodExitEventInfoProfile = new PureMethodExitEventInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    methodExitEvent = mockMethodExitEvent,
    jdiArguments = mockJdiArguments
  )(
    _method = mockMethod,
    _returnValue = mockReturnValue,
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureMethodExitEventInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[MethodExitEventInfoProfile]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducerProfile]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newMethodExitEventInfoProfile(
          _: ScalaVirtualMachine,
          _: MethodExitEvent,
          _: Seq[JDIArgument]
        )(
          _: Method,
          _: Value,
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          mockMethodExitEvent,
          mockJdiArguments,
          *, *, *, *, *, *
        ).returning(expected).once()

        val actual = pureMethodExitEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureMethodExitEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockMethodExitEvent

        val actual = pureMethodExitEventInfoProfile.toJdiInstance

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

        val actual = pureMethodExitEventInfoProfile.method

        actual should be (expected)
      }
    }

    describe("#returnValue") {
      it("should return a new value info profile for the jdi return value") {
        val expected = mock[ValueInfoProfile]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newValueInfoProfile _)
          .expects(mockScalaVirtualMachine, mockReturnValue)
          .returning(expected).once()

        val actual = pureMethodExitEventInfoProfile.returnValue

        actual should be (expected)
      }
    }
  }
}
