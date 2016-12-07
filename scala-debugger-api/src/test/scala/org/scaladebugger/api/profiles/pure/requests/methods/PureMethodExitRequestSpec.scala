package org.scaladebugger.api.profiles.pure.requests.methods
import com.sun.jdi.event.MethodExitEvent
import org.scaladebugger.api.lowlevel.events.EventType.MethodExitEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.methods.{MethodExitManager, MethodExitRequestInfo, PendingMethodExitSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, MethodExitEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureMethodExitRequestSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodExitManager = mock[MethodExitManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MethodExitEvent
  private type EI = MethodExitEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, String, Seq[JDIRequestArgument])
  private type CounterKey = (String, String, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MethodExitEventType
  )

  private class TestPureMethodExitRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureMethodExitRequest {
    override def newMethodExitRequestHelper() = {
      val originalRequestHelper = super.newMethodExitRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val methodExitManager = mockMethodExitManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureMethodExitProfile =
    new TestPureMethodExitRequest(Some(mockRequestHelper))

  describe("PureMethodExitRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

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

          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

          val requestId = expected.get
          val className = "class.name"
          val methodName = "method.name"
          val requestArgs = (className, methodName, Seq(mock[JDIRequestArgument]))
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMethodExitManager.createMethodExitRequestWithId _)
            .expects(requestId, className, methodName, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be (expected)
        }
      }

      describe("#_hasRequest") {
        it("should return the result of checking if a request with matching properties exists") {
          val expected = true

          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

          val className = "class.name"
          val methodName = "method.name"
          val requestArgs = (className, methodName, Seq(mock[JDIRequestArgument]))

          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(expected)
            .once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

          val requestId = "some id"

          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MethodExitRequestInfo(
            requestId = "some id",
            isPending = true,
            className = "some.name",
            methodName = "someName",
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

          val requestId = "some id"

          (mockMethodExitManager.getMethodExitRequestInfoWithId _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MethodExitEventInfo]

          val pureMethodExitProfile = new TestPureMethodExitRequest()
          val requestHelper = pureMethodExitProfile.newMethodExitRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MethodExitEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMethodExitEventInfoProfile _)
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

    describe("#tryGetOrCreateMethodExitRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val className = "some.name"
        val methodName = "someName"
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = (className, methodName, mockJdiRequestArgs)

        // Should create a new request and event pipeline with the pipeline
        // being fed a method name filter as well as the provided input args
        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _).expects(
          requestId,
          MethodNameFilter(methodName) +: mockJdiEventArgs,
          requestArgs
        ).returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureMethodExitProfile.tryGetOrCreateMethodExitRequest(
          className,
          methodName,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#methodExitRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MethodExitRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodExitManager = mock[PendingMethodExitSupportLike]
        val pureMethodExitProfile = new Object with PureMethodExitRequest {
          override protected val methodExitManager = mockMethodExitManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()

        (mockMethodExitManager.pendingMethodExitRequests _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.methodExitRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MethodExitRequestInfo(
            TestRequestId,
            true,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodExitManager = mock[PendingMethodExitSupportLike]
        val pureMethodExitProfile = new Object with PureMethodExitRequest {
          override protected val methodExitManager = mockMethodExitManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        (mockMethodExitManager.pendingMethodExitRequests _).expects()
          .returning(expected).once()

        val actual = pureMethodExitProfile.methodExitRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MethodExitRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()

        val actual = pureMethodExitProfile.methodExitRequests

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.removeMethodExitRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching filename exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.removeMethodExitRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching line number exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.removeMethodExitRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeMethodExitRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeMethodExitRequests(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return None if no request with matching filename exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching line number exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeMethodExitRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllMethodExitRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.removeAllMethodExitRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeAllMethodExitRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodExitManager.removeMethodExitRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodExitProfile.removeAllMethodExitRequests()

        actual should be (expected)
      }
    }

    describe("#isMethodExitRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.isMethodExitRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching clas name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching method name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#isMethodExitRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching class name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching method name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodExitRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodExitManager.methodExitRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodExitProfile.isMethodExitRequestWithArgsPending(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}

