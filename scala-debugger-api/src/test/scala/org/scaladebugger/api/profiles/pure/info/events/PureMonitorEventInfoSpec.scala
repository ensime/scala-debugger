package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi._
import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ObjectInfo, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class PureMonitorEventInfoSpec extends test.ParallelMockFunSpec {
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducer = mock[InfoProducer]
  private val testMonitorEvent = new MonitorEvent(mock[MonitorWaitEvent])

  private val mockJdiRequestArguments = Seq(mock[JDIRequestArgument])
  private val mockJdiEventArguments = Seq(mock[JDIEventArgument])
  private val mockJdiArguments =
    mockJdiRequestArguments ++ mockJdiEventArguments

  private val mockMonitorReference = mock[ObjectReference]
  private val mockMonitorReferenceType = mock[ReferenceType]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadReferenceType = mock[ReferenceType]
  private val mockLocation = mock[Location]

  private val pureMonitorEventInfoProfile = new PureMonitorEventInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducer,
    monitorEvent = testMonitorEvent,
    jdiArguments = mockJdiArguments
  )(
    _monitor = mockMonitorReference,
    _monitorReferenceType = mockMonitorReferenceType,
    _virtualMachine = mockVirtualMachine,
    _thread = mockThreadReference,
    _threadReferenceType = mockThreadReferenceType,
    _location = mockLocation
  )

  describe("PureMonitorEventInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[MonitorEventInfo]

        // Event info producer will be generated in its Java form
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.toJavaInfo _).expects()
          .returning(mockEventInfoProducer).once()

        // Java version of event info producer creates a new event instance
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockEventInfoProducer.newMonitorEventInfoProfile(
          _: ScalaVirtualMachine,
          _: MonitorEvent,
          _: Seq[JDIArgument]
        )(
          _: ObjectReference,
          _: ReferenceType,
          _: VirtualMachine,
          _: ThreadReference,
          _: ReferenceType,
          _: Location
        )).expects(
          mockScalaVirtualMachine,
          testMonitorEvent,
          mockJdiArguments,
          *, *, *, *, *, *
        ).returning(expected).once()

        val actual = pureMonitorEventInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureMonitorEventInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = testMonitorEvent.locatableEvent

        val actual = pureMonitorEventInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#monitor") {
      it("should return a new instance of the object info profile") {
        val expected = mock[ObjectInfo]

        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducer.newObjectInfoProfile(
          _: ScalaVirtualMachine,
          _: ObjectReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockMonitorReference,
          *, *
        ).returning(expected).once()

        val actual = pureMonitorEventInfoProfile.monitor

        actual should be (expected)
      }
    }

    describe("#thread") {
      it("should return a new instance of the thread info profile") {
        val expected = mock[ThreadInfo]

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

        val actual = pureMonitorEventInfoProfile.thread

        actual should be (expected)
      }
    }
  }
}
