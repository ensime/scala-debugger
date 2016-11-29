package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event._
import com.sun.jdi.{Location, ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, ReferenceTypeInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureClassPrepareEventInfoProfileSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockClassPrepareEvent = mock[ClassPrepareEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockReferenceType = mock[ReferenceType]

  private val pureClassPrepareEventInfoProfile = new PureClassPrepareEventInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    classPrepareEvent = mockClassPrepareEvent,
    jdiArguments = mockJdiArguments
  )(
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _referenceType = mockReferenceType
  )

  describe("PureClassPrepareEventInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ClassPrepareEventInfoProfile]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducerProfile]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newClassPrepareEventInfoProfile(
          _: ScalaVirtualMachine,
          _: ClassPrepareEvent,
          _: Seq[JDIArgument]
        )(
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockClassPrepareEvent,
          mockJdiArguments,
          *, *, *, *
        ).returning(expected).once()

        val actual = pureClassPrepareEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureClassPrepareEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassPrepareEvent

        val actual = pureClassPrepareEventInfoProfile.toJdiInstance

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

        val actual = pureClassPrepareEventInfoProfile.thread

        actual should be (expected)
      }
    }

    describe("#referenceType") {
      it("should return a new instance of the reference type info profile") {
        val expected = mock[ReferenceTypeInfoProfile]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newReferenceTypeInfoProfile(
          _: ScalaVirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockReferenceType
        ).returning(expected).once()

        val actual = pureClassPrepareEventInfoProfile.referenceType

        actual should be (expected)
      }
    }
  }
}
