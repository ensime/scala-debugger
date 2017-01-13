package org.scaladebugger.api.profiles.pure.requests.classes
import com.sun.jdi.event.ClassUnloadEvent
import org.scaladebugger.api.lowlevel.classes.{ClassUnloadManager, ClassUnloadRequestInfo, PendingClassUnloadSupportLike}
import org.scaladebugger.api.lowlevel.events.EventType.ClassUnloadEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{ClassUnloadEventInfo, EventInfoProducer}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureClassUnloadRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassUnloadManager = mock[ClassUnloadManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ClassUnloadEvent
  private type EI = ClassUnloadEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ClassUnloadEventType
  )

  private class TestPureClassUnloadRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureClassUnloadRequest {
    override def newClassUnloadRequestHelper() = {
      val originalRequestHelper = super.newClassUnloadRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val classUnloadManager = mockClassUnloadManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureClassUnloadProfile =
    new TestPureClassUnloadRequest(Some(mockRequestHelper))

  describe("PureClassUnloadRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

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

          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockClassUnloadManager.createClassUnloadRequestWithId _)
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

          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ClassUnloadRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockClassUnloadManager.classUnloadRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockClassUnloadManager.getClassUnloadRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ClassUnloadRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockClassUnloadManager.classUnloadRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockClassUnloadManager.getClassUnloadRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val requestId = "some id"

          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(ClassUnloadRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val requestId = "some id"

          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[ClassUnloadEventInfo]

          val pureClassUnloadProfile = new TestPureClassUnloadRequest()
          val requestHelper = pureClassUnloadProfile.newClassUnloadRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[ClassUnloadEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultClassUnloadEventInfo _)
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

    describe("#tryGetOrCreateClassUnloadRequestWithData") {
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

        val actual = pureClassUnloadProfile.tryGetOrCreateClassUnloadRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#classUnloadRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ClassUnloadRequestInfo(TestRequestId, false)
        )

        val mockClassUnloadManager = mock[PendingClassUnloadSupportLike]
        val pureClassUnloadProfile = new Object with PureClassUnloadRequest {
          override protected val classUnloadManager = mockClassUnloadManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockClassUnloadManager.getClassUnloadRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockClassUnloadManager.pendingClassUnloadRequests _).expects()
          .returning(Nil).once()

        val actual = pureClassUnloadProfile.classUnloadRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ClassUnloadRequestInfo(TestRequestId, true)
        )

        val mockClassUnloadManager = mock[PendingClassUnloadSupportLike]
        val pureClassUnloadProfile = new Object with PureClassUnloadRequest {
          override protected val classUnloadManager = mockClassUnloadManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Nil).once()

        (mockClassUnloadManager.pendingClassUnloadRequests _).expects()
          .returning(expected).once()

        val actual = pureClassUnloadProfile.classUnloadRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ClassUnloadRequestInfo(TestRequestId, false)
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockClassUnloadManager.getClassUnloadRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureClassUnloadProfile.classUnloadRequests

        actual should be(expected)
      }
    }

    describe("#removeClassUnloadRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Nil).once()

        val actual = pureClassUnloadProfile.removeClassUnloadRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureClassUnloadProfile.removeClassUnloadRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureClassUnloadProfile.removeClassUnloadRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureClassUnloadProfile.removeClassUnloadRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllClassUnloadRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Nil).once()

        val actual = pureClassUnloadProfile.removeAllClassUnloadRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureClassUnloadProfile.removeAllClassUnloadRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureClassUnloadProfile.removeAllClassUnloadRequests()

        actual should be(expected)
      }
    }


    describe("#isClassUnloadRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(Nil).once()

        val actual = pureClassUnloadProfile.isClassUnloadRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureClassUnloadProfile.isClassUnloadRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureClassUnloadProfile.isClassUnloadRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassUnloadRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassUnloadManager.classUnloadRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureClassUnloadProfile.isClassUnloadRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}

