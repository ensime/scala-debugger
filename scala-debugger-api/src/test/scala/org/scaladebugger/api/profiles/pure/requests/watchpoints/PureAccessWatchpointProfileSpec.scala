package org.scaladebugger.api.profiles.pure.requests.watchpoints
import acyclic.file
import com.sun.jdi.event.{AccessWatchpointEvent, Event}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.AccessWatchpointEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.watchpoints.{AccessWatchpointManager, AccessWatchpointRequestInfo, PendingAccessWatchpointSupportLike}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.{Constants, RequestHelper}
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.AccessWatchpointEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureAccessWatchpointProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockAccessWatchpointManager = mock[AccessWatchpointManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = AccessWatchpointEvent
  private type EI = AccessWatchpointEventInfoProfile
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, String, Seq[JDIRequestArgument])
  private type CounterKey = (String, String, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = AccessWatchpointEventType
  )

  private val mockRequestHelper = mock[CustomTestRequestHelper]

  private val pureAccessWatchpointProfile = new Object with PureAccessWatchpointProfile {
    override protected def newAccessWatchpointRequestHelper() = mockRequestHelper
    override protected val accessWatchpointManager = mockAccessWatchpointManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureAccessWatchpointProfile") {
    describe("#tryGetOrCreateAccessWatchpointRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val className = "some.name"
        val fieldName = "someName"
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = (className, fieldName, mockJdiRequestArgs)

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureAccessWatchpointProfile.tryGetOrCreateAccessWatchpointRequest(
          className,
          fieldName,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#accessWatchpointRequests") {
      it("should include all active requests") {
        val expected = Seq(
          AccessWatchpointRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someFieldName"
          )
        )

        val mockAccessWatchpointManager = mock[PendingAccessWatchpointSupportLike]
        val pureAccessWatchpointProfile = new Object with PureAccessWatchpointProfile {
          override protected val accessWatchpointManager = mockAccessWatchpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()

        (mockAccessWatchpointManager.pendingAccessWatchpointRequests _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.accessWatchpointRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          AccessWatchpointRequestInfo(
            TestRequestId,
            true,
            "some.class.name",
            "someFieldName"
          )
        )

        val mockAccessWatchpointManager = mock[PendingAccessWatchpointSupportLike]
        val pureAccessWatchpointProfile = new Object with PureAccessWatchpointProfile {
          override protected val accessWatchpointManager = mockAccessWatchpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        (mockAccessWatchpointManager.pendingAccessWatchpointRequests _).expects()
          .returning(expected).once()

        val actual = pureAccessWatchpointProfile.accessWatchpointRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          AccessWatchpointRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someFieldName"
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()

        val actual = pureAccessWatchpointProfile.accessWatchpointRequests

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching filename exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching line number exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return None if no request with matching filename exists") {
        val expected = None
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching line number exists") {
        val expected = None
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAccessWatchpointRequestWithArgs(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllAccessWatchpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.removeAllAccessWatchpointRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAllAccessWatchpointRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureAccessWatchpointProfile.removeAllAccessWatchpointRequests()

        actual should be (expected)
      }
    }

    describe("#isAccessWatchpointRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching class name exists") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching field name exists") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }
    }

    describe("#isAccessWatchpointRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching class name exists") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching field name exists") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          AccessWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockAccessWatchpointManager.accessWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureAccessWatchpointProfile.isAccessWatchpointRequestWithArgsPending(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
