package org.scaladebugger.api.profiles.pure.requests.threads
import acyclic.file
import com.sun.jdi.event.ThreadDeathEvent
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.ThreadDeathEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.threads.{PendingThreadDeathSupportLike, ThreadDeathManager, ThreadDeathRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducerProfile, ThreadDeathEventInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureThreadDeathProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadDeathManager = mock[ThreadDeathManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ThreadDeathEvent
  private type EI = ThreadDeathEventInfoProfile
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ThreadDeathEventType
  )

  private class TestPureThreadDeathProfile(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureThreadDeathProfile {
    override def newThreadDeathRequestHelper() = {
      val originalRequestHelper = super.newThreadDeathRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val threadDeathManager = mockThreadDeathManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureThreadDeathProfile =
    new TestPureThreadDeathProfile(Some(mockRequestHelper))

  describe("PureThreadDeathProfile") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

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

          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockThreadDeathManager.createThreadDeathRequestWithId _)
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

          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ThreadDeathRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockThreadDeathManager.threadDeathRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockThreadDeathManager.getThreadDeathRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ThreadDeathRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockThreadDeathManager.threadDeathRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockThreadDeathManager.getThreadDeathRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val requestId = "some id"

          (mockThreadDeathManager.removeThreadDeathRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(ThreadDeathRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val requestId = "some id"

          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[ThreadDeathEventInfoProfile]

          val pureThreadDeathProfile = new TestPureThreadDeathProfile()
          val requestHelper = pureThreadDeathProfile.newThreadDeathRequestHelper()

          val mockEventProducer = mock[EventInfoProducerProfile]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[ThreadDeathEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultThreadDeathEventInfoProfile _)
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

    describe("#tryGetOrCreateThreadDeathRequestWithData") {
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

        val actual = pureThreadDeathProfile.tryGetOrCreateThreadDeathRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#threadDeathRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ThreadDeathRequestInfo(TestRequestId, false)
        )

        val mockThreadDeathManager = mock[PendingThreadDeathSupportLike]
        val pureThreadDeathProfile = new Object with PureThreadDeathProfile {
          override protected val threadDeathManager = mockThreadDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockThreadDeathManager.getThreadDeathRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockThreadDeathManager.pendingThreadDeathRequests _).expects()
          .returning(Nil).once()

        val actual = pureThreadDeathProfile.threadDeathRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ThreadDeathRequestInfo(TestRequestId, true)
        )

        val mockThreadDeathManager = mock[PendingThreadDeathSupportLike]
        val pureThreadDeathProfile = new Object with PureThreadDeathProfile {
          override protected val threadDeathManager = mockThreadDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Nil).once()

        (mockThreadDeathManager.pendingThreadDeathRequests _).expects()
          .returning(expected).once()

        val actual = pureThreadDeathProfile.threadDeathRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ThreadDeathRequestInfo(TestRequestId, false)
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockThreadDeathManager.getThreadDeathRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureThreadDeathProfile.threadDeathRequests

        actual should be (expected)
      }
    }

    describe("#removeThreadDeathRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadDeathProfile.removeThreadDeathRequestWithArgs()

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadDeathProfile.removeThreadDeathRequestWithArgs()

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadDeathManager.removeThreadDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadDeathProfile.removeThreadDeathRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadDeathManager.removeThreadDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadDeathProfile.removeThreadDeathRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllThreadDeathRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadDeathProfile.removeAllThreadDeathRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadDeathManager.removeThreadDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadDeathProfile.removeAllThreadDeathRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockThreadDeathManager.removeThreadDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureThreadDeathProfile.removeAllThreadDeathRequests()

        actual should be (expected)
      }
    }

    describe("#isThreadDeathRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureThreadDeathProfile.isThreadDeathRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadDeathProfile.isThreadDeathRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadDeathProfile.isThreadDeathRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ThreadDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockThreadDeathManager.threadDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockThreadDeathManager.getThreadDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureThreadDeathProfile.isThreadDeathRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}

