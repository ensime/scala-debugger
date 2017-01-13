package org.scaladebugger.api.profiles.pure.requests.threads

import com.sun.jdi.event.ThreadStartEvent
import org.scaladebugger.api.lowlevel.events.EventType.ThreadStartEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.threads.{PendingThreadStartSupportLike, ThreadStartManager, ThreadStartRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, ThreadStartEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureThreadStartRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadStartManager = mock[ThreadStartManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ThreadStartEvent
  private type EI = ThreadStartEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ThreadStartEventType
  )

  private class TestPureThreadStartRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureThreadStartRequest {
    override def newThreadStartRequestHelper() = {
      val originalRequestHelper = super.newThreadStartRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val threadStartManager = mockThreadStartManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureThreadStartProfile =
    new TestPureThreadStartRequest(Some(mockRequestHelper))

  describe("PureThreadStartRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId1 = requestHelper._newRequestId()
          val requestId2 = requestHelper._newRequestId()

          requestId1 shouldBe a [String]
          requestId2 shouldBe a [String]
          requestId1 should not be (requestId2)
        }
      }

      describe("#_newRequest") {
        it("should create a new request with the provided args and id") {
          val expected = Success("some id")

          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockThreadStartManager.createThreadStartRequestWithId _)
            .expects(requestId, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be (expected)
        }
      }

      describe("#_hasRequest") {
        it("should return true if a request exists with matching request arguments") {
          val expected = true

          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ThreadStartRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockThreadStartManager.threadStartRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockThreadStartManager.getThreadStartRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ThreadStartRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockThreadStartManager.threadStartRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockThreadStartManager.getThreadStartRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId = "some id"

          (mockThreadStartManager.removeThreadStartRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(ThreadStartRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val requestId = "some id"

          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[ThreadStartEventInfo]

          val pureThreadStartProfile = new TestPureThreadStartRequest()
          val requestHelper = pureThreadStartProfile.newThreadStartRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[ThreadStartEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultThreadStartEventInfo _)
            .expects(mockScalaVirtualMachine, mockEvent, mockJdiArgs)
            .returning(expected).once()

          val actual = requestHelper._newEventInfo(
            mockScalaVirtualMachine,
            mockEvent,
            mockJdiArgs
          )

          actual should be (expected)
        }
      }
    }

    describe("#tryGetOrCreateThreadStartRequestWithData") {
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

        val actual = pureThreadStartProfile.tryGetOrCreateThreadStartRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#threadStartRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ThreadStartRequestInfo(TestRequestId, false)
        )

        val mockThreadStartManager = mock[PendingThreadStartSupportLike]
        val pureThreadStartProfile = new Object with PureThreadStartRequest {
          override protected val threadStartManager = mockThreadStartManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockThreadStartManager.getThreadStartRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockThreadStartManager.pendingThreadStartRequests _).expects()
          .returning(Nil).once()

        val actual = pureThreadStartProfile.threadStartRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ThreadStartRequestInfo(TestRequestId, true)
        )

        val mockThreadStartManager = mock[PendingThreadStartSupportLike]
        val pureThreadStartProfile = new Object with PureThreadStartRequest {
          override protected val threadStartManager = mockThreadStartManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Nil).once()

        (mockThreadStartManager.pendingThreadStartRequests _).expects()
          .returning(expected).once()

        val actual = pureThreadStartProfile.threadStartRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ThreadStartRequestInfo(TestRequestId, false)
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockThreadStartManager.getThreadStartRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureThreadStartProfile.threadStartRequests

        actual should be (expected)
      }
    }

    describe("#removeThreadStartRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadStartProfile.removeThreadStartRequestWithArgs()

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadStartProfile.removeThreadStartRequestWithArgs()

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadStartManager.removeThreadStartRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadStartProfile.removeThreadStartRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadStartManager.removeThreadStartRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadStartProfile.removeThreadStartRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllThreadStartRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadStartProfile.removeAllThreadStartRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadStartManager.removeThreadStartRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadStartProfile.removeAllThreadStartRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadStartManager.removeThreadStartRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadStartProfile.removeAllThreadStartRequests()

        actual should be (expected)
      }
    }

    describe("#isThreadStartRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadStartProfile.isThreadStartRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadStartProfile.isThreadStartRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadStartProfile.isThreadStartRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadStartRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadStartManager.threadStartRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadStartManager.getThreadStartRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadStartProfile.isThreadStartRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}

