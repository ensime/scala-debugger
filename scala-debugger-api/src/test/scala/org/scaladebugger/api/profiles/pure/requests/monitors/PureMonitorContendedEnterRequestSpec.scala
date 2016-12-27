package org.scaladebugger.api.profiles.pure.requests.monitors

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scaladebugger.api.lowlevel.events.EventType.MonitorContendedEnterEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnterManager, MonitorContendedEnterRequestInfo, PendingMonitorContendedEnterSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, MonitorContendedEnterEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureMonitorContendedEnterRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnterManager =
    mock[MonitorContendedEnterManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MonitorContendedEnterEvent
  private type EI = MonitorContendedEnterEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MonitorContendedEnterEventType
  )

  private class TestPureMonitorContendedEnterRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureMonitorContendedEnterRequest {
    override def newMonitorContendedEnterRequestHelper() = {
      val originalRequestHelper = super.newMonitorContendedEnterRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val monitorContendedEnterManager = mockMonitorContendedEnterManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureMonitorContendedEnterProfile =
    new TestPureMonitorContendedEnterRequest(Some(mockRequestHelper))

  describe("PureMonitorContendedEnterRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId1 = requestHelper._newRequestId()
          val requestId2 = requestHelper._newRequestId()

          requestId1 shouldBe a[String]
          requestId2 shouldBe a[String]
          requestId1 should not be (requestId2)
        }
      }

      describe("#_newRequest") {
        it("should create a new request with the provided args and id") {
          val expected = Success("some id")

          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(requestId, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be(expected)
        }
      }

      describe("#_hasRequest") {
        it("should return true if a request exists with matching request arguments") {
          val expected = true

          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorContendedEnterRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorContendedEnterRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId = "some id"

          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MonitorContendedEnterRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val requestId = "some id"

          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MonitorContendedEnterEventInfo]

          val pureMonitorContendedEnterProfile = new TestPureMonitorContendedEnterRequest()
          val requestHelper = pureMonitorContendedEnterProfile.newMonitorContendedEnterRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MonitorContendedEnterEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMonitorContendedEnterEventInfoProfile _)
            .expects(mockScalaVirtualMachine, mockEvent, mockJdiArgs)
            .returning(expected).once()

          val actual = requestHelper._newEventInfo(
            mockScalaVirtualMachine,
            mockEvent,
            mockJdiArgs
          )

          actual should be(expected)
        }
      }
    }

    describe("#tryGetOrCreateMonitorContendedEnterRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = mockJdiRequestArgs

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureMonitorContendedEnterProfile.tryGetOrCreateMonitorContendedEnterRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#monitorContendedEnterRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MonitorContendedEnterRequestInfo(TestRequestId, false)
        )

        val mockMonitorContendedEnterManager = mock[PendingMonitorContendedEnterSupportLike]
        val pureMonitorContendedEnterProfile = new Object with PureMonitorContendedEnterRequest {
          override protected val monitorContendedEnterManager = mockMonitorContendedEnterManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockMonitorContendedEnterManager.pendingMonitorContendedEnterRequests _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnterProfile.monitorContendedEnterRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MonitorContendedEnterRequestInfo(TestRequestId, true)
        )

        val mockMonitorContendedEnterManager = mock[PendingMonitorContendedEnterSupportLike]
        val pureMonitorContendedEnterProfile = new Object with PureMonitorContendedEnterRequest {
          override protected val monitorContendedEnterManager = mockMonitorContendedEnterManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(Nil).once()

        (mockMonitorContendedEnterManager.pendingMonitorContendedEnterRequests _).expects()
          .returning(expected).once()

        val actual = pureMonitorContendedEnterProfile.monitorContendedEnterRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MonitorContendedEnterRequestInfo(TestRequestId, false)
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureMonitorContendedEnterProfile.monitorContendedEnterRequests

        actual should be(expected)
      }
    }

    describe("#removeMonitorContendedEnterRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnterProfile.removeMonitorContendedEnterRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnterProfile.removeMonitorContendedEnterRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnterProfile.removeMonitorContendedEnterRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnterProfile.removeMonitorContendedEnterRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllMonitorContendedEnterRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnterProfile.removeAllMonitorContendedEnterRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnterProfile.removeAllMonitorContendedEnterRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnterProfile.removeAllMonitorContendedEnterRequests()

        actual should be(expected)
      }
    }

    describe("#isMonitorContendedEnterRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnterProfile.isMonitorContendedEnterRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnterProfile.isMonitorContendedEnterRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnterProfile.isMonitorContendedEnterRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnterRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnterProfile.isMonitorContendedEnterRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}
