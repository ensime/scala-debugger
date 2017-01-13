package org.scaladebugger.api.profiles.pure.requests.methods

import com.sun.jdi.event.MethodEntryEvent
import org.scaladebugger.api.lowlevel.events.EventType.MethodEntryEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.methods.{MethodEntryManager, MethodEntryRequestInfo, PendingMethodEntrySupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, MethodEntryEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureMethodEntryRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodEntryManager = mock[MethodEntryManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = MethodEntryEvent
  private type EI = MethodEntryEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, String, Seq[JDIRequestArgument])
  private type CounterKey = (String, String, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = MethodEntryEventType
  )

  private class TestPureMethodEntryRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureMethodEntryRequest {
    override def newMethodEntryRequestHelper() = {
      val originalRequestHelper = super.newMethodEntryRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val methodEntryManager = mockMethodEntryManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureMethodEntryProfile =
    new TestPureMethodEntryRequest(Some(mockRequestHelper))

  describe("PureMethodEntryRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

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

          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

          val requestId = expected.get
          val className = "class.name"
          val methodName = "method.name"
          val requestArgs = (className, methodName, Seq(mock[JDIRequestArgument]))
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockMethodEntryManager.createMethodEntryRequestWithId _)
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

          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

          val className = "class.name"
          val methodName = "method.name"
          val requestArgs = (className, methodName, Seq(mock[JDIRequestArgument]))

          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(expected)
            .once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

          val requestId = "some id"

          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(MethodEntryRequestInfo(
            requestId = "some id",
            isPending = true,
            className = "some.name",
            methodName = "someName",
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

          val requestId = "some id"

          (mockMethodEntryManager.getMethodEntryRequestInfoWithId _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[MethodEntryEventInfo]

          val pureMethodEntryProfile = new TestPureMethodEntryRequest()
          val requestHelper = pureMethodEntryProfile.newMethodEntryRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[MethodEntryEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultMethodEntryEventInfo _)
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

    describe("#tryGetOrCreateMethodEntryRequestWithData") {
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

        val actual = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequest(
          className,
          methodName,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#methodEntryRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodEntryManager = mock[PendingMethodEntrySupportLike]
        val pureMethodEntryProfile = new Object with PureMethodEntryRequest {
          override protected val methodEntryManager = mockMethodEntryManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()

        (mockMethodEntryManager.pendingMethodEntryRequests _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            true,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodEntryManager = mock[PendingMethodEntrySupportLike]
        val pureMethodEntryProfile = new Object with PureMethodEntryRequest {
          override protected val methodEntryManager = mockMethodEntryManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        (mockMethodEntryManager.pendingMethodEntryRequests _).expects()
          .returning(expected).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllMethodEntryRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }
    }

    describe("#isMethodEntryRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#isMethodEntryRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
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
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}

