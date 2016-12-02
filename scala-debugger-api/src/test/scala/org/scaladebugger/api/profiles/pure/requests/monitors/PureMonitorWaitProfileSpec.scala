package org.scaladebugger.api.profiles.pure.requests.monitors

import com.sun.jdi.event.MonitorWaitEvent
import org.scaladebugger.api.lowlevel.events.EventType.MonitorWaitEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitManager, MonitorWaitRequestInfo, PendingMonitorWaitSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducerProfile, MonitorWaitEventInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureMonitorWaitProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorWaitManager =
    mock[MonitorWaitManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MonitorWaitEvent
  private type EI = MonitorWaitEventInfoProfile
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MonitorWaitEventType
  )

  private class TestPureMonitorWaitProfile(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureMonitorWaitProfile {
    override def newMonitorWaitRequestHelper() = {
      val originalRequestHelper = super.newMonitorWaitRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val monitorWaitManager = mockMonitorWaitManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureMonitorWaitProfile =
    new TestPureMonitorWaitProfile(Some(mockRequestHelper))

  describe("PureMonitorWaitProfile") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

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

          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
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

          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorWaitRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockMonitorWaitManager.monitorWaitRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorWaitRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockMonitorWaitManager.monitorWaitRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val requestId = "some id"

          (mockMonitorWaitManager.removeMonitorWaitRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MonitorWaitRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val requestId = "some id"

          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MonitorWaitEventInfoProfile]

          val pureMonitorWaitProfile = new TestPureMonitorWaitProfile()
          val requestHelper = pureMonitorWaitProfile.newMonitorWaitRequestHelper()

          val mockEventProducer = mock[EventInfoProducerProfile]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MonitorWaitEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMonitorWaitEventInfoProfile _)
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

    describe("#tryGetOrCreateMonitorWaitRequestWithData") {
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

        val actual = pureMonitorWaitProfile.tryGetOrCreateMonitorWaitRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#monitorWaitRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MonitorWaitRequestInfo(TestRequestId, false)
        )

        val mockMonitorWaitManager = mock[PendingMonitorWaitSupportLike]
        val pureMonitorWaitProfile = new Object with PureMonitorWaitProfile {
          override protected val monitorWaitManager = mockMonitorWaitManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockMonitorWaitManager.pendingMonitorWaitRequests _).expects()
          .returning(Nil).once()

        val actual = pureMonitorWaitProfile.monitorWaitRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MonitorWaitRequestInfo(TestRequestId, true)
        )

        val mockMonitorWaitManager = mock[PendingMonitorWaitSupportLike]
        val pureMonitorWaitProfile = new Object with PureMonitorWaitProfile {
          override protected val monitorWaitManager = mockMonitorWaitManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(Nil).once()

        (mockMonitorWaitManager.pendingMonitorWaitRequests _).expects()
          .returning(expected).once()

        val actual = pureMonitorWaitProfile.monitorWaitRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MonitorWaitRequestInfo(TestRequestId, false)
        )

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureMonitorWaitProfile.monitorWaitRequests

        actual should be(expected)
      }
    }

    describe("#removeMonitorWaitRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorWaitProfile.removeMonitorWaitRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorWaitProfile.removeMonitorWaitRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitManager.removeMonitorWaitRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorWaitProfile.removeMonitorWaitRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitManager.removeMonitorWaitRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorWaitProfile.removeMonitorWaitRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllMonitorWaitRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorWaitProfile.removeAllMonitorWaitRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitManager.removeMonitorWaitRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorWaitProfile.removeAllMonitorWaitRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitManager.removeMonitorWaitRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorWaitProfile.removeAllMonitorWaitRequests()

        actual should be(expected)
      }
    }

    describe("#isMonitorWaitRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMonitorWaitProfile.isMonitorWaitRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorWaitProfile.isMonitorWaitRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorWaitProfile.isMonitorWaitRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitManager.monitorWaitRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitManager.getMonitorWaitRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorWaitProfile.isMonitorWaitRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}
