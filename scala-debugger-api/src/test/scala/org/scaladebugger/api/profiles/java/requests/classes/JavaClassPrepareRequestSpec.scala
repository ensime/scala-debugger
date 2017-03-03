package org.scaladebugger.api.profiles.java.requests.classes

import com.sun.jdi.event.ClassPrepareEvent
import org.scaladebugger.api.lowlevel.classes.{ClassPrepareManager, ClassPrepareRequestInfo, PendingClassPrepareSupportLike}
import org.scaladebugger.api.lowlevel.events.EventType.ClassPrepareEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{ClassPrepareEventInfo, EventInfoProducer}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class JavaClassPrepareRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassPrepareManager = mock[ClassPrepareManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ClassPrepareEvent
  private type EI = ClassPrepareEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ClassPrepareEventType
  )

  private class TestJavaClassPrepareRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends JavaClassPrepareRequest {
    override def newClassPrepareRequestHelper() = {
      val originalRequestHelper = super.newClassPrepareRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val classPrepareManager = mockClassPrepareManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val javaClassPrepareProfile =
    new TestJavaClassPrepareRequest(Some(mockRequestHelper))

  describe("JavaClassPrepareRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

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

          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockClassPrepareManager.createClassPrepareRequestWithId _)
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

          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ClassPrepareRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockClassPrepareManager.classPrepareRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockClassPrepareManager.getClassPrepareRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = ClassPrepareRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockClassPrepareManager.classPrepareRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockClassPrepareManager.getClassPrepareRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be(expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val requestId = "some id"

          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(ClassPrepareRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val requestId = "some id"

          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be(expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[ClassPrepareEventInfo]

          val javaClassPrepareProfile = new TestJavaClassPrepareRequest()
          val requestHelper = javaClassPrepareProfile.newClassPrepareRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[ClassPrepareEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultClassPrepareEventInfo _)
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

    describe("#tryGetOrCreateClassPrepareRequestWithData") {
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

        val actual = javaClassPrepareProfile.tryGetOrCreateClassPrepareRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an[IdentityPipeline[EIData]]
      }
    }

    describe("#classPrepareRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ClassPrepareRequestInfo(TestRequestId, false)
        )

        val mockClassPrepareManager = mock[PendingClassPrepareSupportLike]
        val javaClassPrepareProfile = new Object with JavaClassPrepareRequest {
          override protected val classPrepareManager = mockClassPrepareManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockClassPrepareManager.getClassPrepareRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockClassPrepareManager.pendingClassPrepareRequests _).expects()
          .returning(Nil).once()

        val actual = javaClassPrepareProfile.classPrepareRequests

        actual should be(expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ClassPrepareRequestInfo(TestRequestId, true)
        )

        val mockClassPrepareManager = mock[PendingClassPrepareSupportLike]
        val javaClassPrepareProfile = new Object with JavaClassPrepareRequest {
          override protected val classPrepareManager = mockClassPrepareManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Nil).once()

        (mockClassPrepareManager.pendingClassPrepareRequests _).expects()
          .returning(expected).once()

        val actual = javaClassPrepareProfile.classPrepareRequests

        actual should be(expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ClassPrepareRequestInfo(TestRequestId, false)
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockClassPrepareManager.getClassPrepareRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = javaClassPrepareProfile.classPrepareRequests

        actual should be(expected)
      }
    }

    describe("#removeClassPrepareRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Nil).once()

        val actual = javaClassPrepareProfile.removeClassPrepareRequestWithArgs()

        actual should be(expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaClassPrepareProfile.removeClassPrepareRequestWithArgs()

        actual should be(expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaClassPrepareProfile.removeClassPrepareRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaClassPrepareProfile.removeClassPrepareRequestWithArgs(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }

    describe("#removeAllClassPrepareRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Nil).once()

        val actual = javaClassPrepareProfile.removeAllClassPrepareRequests()

        actual should be(expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaClassPrepareProfile.removeAllClassPrepareRequests()

        actual should be(expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = javaClassPrepareProfile.removeAllClassPrepareRequests()

        actual should be(expected)
      }
    }


    describe("#isClassPrepareRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(Nil).once()

        val actual = javaClassPrepareProfile.isClassPrepareRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaClassPrepareProfile.isClassPrepareRequestWithArgsPending()

        actual should be(expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaClassPrepareProfile.isClassPrepareRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ClassPrepareRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockClassPrepareManager.classPrepareRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockClassPrepareManager.getClassPrepareRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = javaClassPrepareProfile.isClassPrepareRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be(expected)
      }
    }
  }
}

