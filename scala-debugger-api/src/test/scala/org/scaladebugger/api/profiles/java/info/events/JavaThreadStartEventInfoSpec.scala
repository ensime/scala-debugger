package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi.event._
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaThreadStartEventInfoSpec extends ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockThreadStartEvent = mock[ThreadStartEvent]

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]

  private val javaThreadStartEventInfoProfile = new JavaThreadStartEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    threadStartEvent = mockThreadStartEvent,
    jdiArguments = mockJdiArguments
  )(
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType
  )

  describe("JavaThreadStartEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ThreadStartEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newThreadStartEventInfo(
          _: ScalaVirtualMachine,
          _: ThreadStartEvent,
          _: Seq[JDIArgument]
        )(
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockThreadStartEvent,
          mockJdiArguments,
          *, *, *
        ).returning(expected).once()

        val actual = javaThreadStartEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaThreadStartEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockThreadStartEvent

        val actual = javaThreadStartEventInfoProfile.toJdiInstance

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

        val actual = javaThreadStartEventInfoProfile.thread

        actual should be (expected)
      }
    }
  }
}
