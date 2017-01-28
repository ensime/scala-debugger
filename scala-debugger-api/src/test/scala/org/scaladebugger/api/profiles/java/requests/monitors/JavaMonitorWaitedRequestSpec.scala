package org.scaladebugger.api.profiles.java.requests.monitors

import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.lowlevel.events.EventType.MonitorWaitedEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitedManager, MonitorWaitedRequestInfo, PendingMonitorWaitedSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, MonitorWaitedEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class JavaMonitorWaitedRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorWaitedManager =
    mock[MonitorWaitedManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MonitorWaitedEvent
  private type EI = MonitorWaitedEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MonitorWaitedEventType
  )

  private class TestJavaMonitorWaitedRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends JavaMonitorWaitedRequest {
    override def newMonitorWaitedRequestHelper() = {
      val originalRequestHelper = super.newMonitorWaitedRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val monitorWaitedManager = mockMonitorWaitedManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val javaMonitorWaitedProfile =
    new TestJavaMonitorWaitedRequest(Some(mockRequestHelper))

  describe("JavaMonitorWaitedRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

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

          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMonitorWaitedManager.createMonitorWaitedRequestWithId _)
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

          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorWaitedRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = MonitorWaitedRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val requestId = "some id"

          (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MonitorWaitedRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val requestId = "some id"

          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MonitorWaitedEventInfo]

          val javaMonitorWaitedProfile = new TestJavaMonitorWaitedRequest()
          val requestHelper = javaMonitorWaitedProfile.newMonitorWaitedRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MonitorWaitedEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMonitorWaitedEventInfo _)
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

    describe("#tryGetOrCreateMonitorWaitedRequestWithData") {
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

        val actual = javaMonitorWaitedProfile.tryGetOrCreateMonitorWaitedRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#monitorWaitedRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MonitorWaitedRequestInfo(TestRequestId, false)
        )

        val mockMonitorWaitedManager = mock[PendingMonitorWaitedSupportLike]
        val javaMonitorWaitedProfile = new Object with JavaMonitorWaitedRequest {
          override protected val monitorWaitedManager = mockMonitorWaitedManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockMonitorWaitedManager.pendingMonitorWaitedRequests _).expects()
          .returning(Nil).once()

        val actual = javaMonitorWaitedProfile.monitorWaitedRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MonitorWaitedRequestInfo(TestRequestId, true)
        )

        val mockMonitorWaitedManager = mock[PendingMonitorWaitedSupportLike]
        val javaMonitorWaitedProfile = new Object with JavaMonitorWaitedRequest {
          override protected val monitorWaitedManager = mockMonitorWaitedManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(Nil).once()

        (mockMonitorWaitedManager.pendingMonitorWaitedRequests _).expects()
          .returning(expected).once()

        val actual = javaMonitorWaitedProfile.monitorWaitedRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MonitorWaitedRequestInfo(TestRequestId, false)
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = javaMonitorWaitedProfile.monitorWaitedRequests

        actual should be(expected)
      }
    }

    describe("#removeMonitorWaitedRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = javaMonitorWaitedProfile.removeMonitorWaitedRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaMonitorWaitedProfile.removeMonitorWaitedRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaMonitorWaitedProfile.removeMonitorWaitedRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaMonitorWaitedProfile.removeMonitorWaitedRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllMonitorWaitedRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = javaMonitorWaitedProfile.removeAllMonitorWaitedRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaMonitorWaitedProfile.removeAllMonitorWaitedRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorWaitedManager.removeMonitorWaitedRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaMonitorWaitedProfile.removeAllMonitorWaitedRequests()

        actual should be(expected)
      }
    }

    describe("#isMonitorWaitedRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(Nil).once()

        val actual = javaMonitorWaitedProfile.isMonitorWaitedRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaMonitorWaitedProfile.isMonitorWaitedRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaMonitorWaitedProfile.isMonitorWaitedRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorWaitedRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorWaitedManager.monitorWaitedRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorWaitedManager.getMonitorWaitedRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaMonitorWaitedProfile.isMonitorWaitedRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}
