package org.scaladebugger.api.profiles.pure.requests.watchpoints
import com.sun.jdi.event.{Event, ModificationWatchpointEvent}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.ModificationWatchpointEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.watchpoints.{ModificationWatchpointManager, ModificationWatchpointRequestInfo, PendingModificationWatchpointSupportLike}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, ModificationWatchpointEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureModificationWatchpointRequestSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val stubClassManager = stub[ClassManager]
  private val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ModificationWatchpointEvent
  private type EI = ModificationWatchpointEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, String, Seq[JDIRequestArgument])
  private type CounterKey = (String, String, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ModificationWatchpointEventType
  )

  private class TestPureModificationWatchpointRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureModificationWatchpointRequest {
    override def newModificationWatchpointRequestHelper() = {
      val originalRequestHelper = super.newModificationWatchpointRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val modificationWatchpointManager = mockModificationWatchpointManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureModificationWatchpointProfile =
    new TestPureModificationWatchpointRequest(Some(mockRequestHelper))

  describe("PureModificationWatchpointRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

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

          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

          val requestId = expected.get
          val className = "class.name"
          val fieldName = "field.name"
          val requestArgs = (className, fieldName, Seq(mock[JDIRequestArgument]))
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
            .expects(requestId, className, fieldName, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be (expected)
        }
      }

      describe("#_hasRequest") {
        it("should return the result of checking if a request with matching properties exists") {
          val expected = true

          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

          val className = "class.name"
          val fieldName = "field.name"
          val requestArgs = (className, fieldName, Seq(mock[JDIRequestArgument]))

          (mockModificationWatchpointManager.hasModificationWatchpointRequest _)
            .expects(className, fieldName)
            .returning(expected)
            .once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

          val requestId = "some id"

          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(ModificationWatchpointRequestInfo(
            requestId = "some id",
            isPending = true,
            className = "some.name",
            fieldName = "someName",
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

          val requestId = "some id"

          (mockModificationWatchpointManager.getModificationWatchpointRequestInfoWithId _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[ModificationWatchpointEventInfo]

          val pureModificationWatchpointProfile = new TestPureModificationWatchpointRequest()
          val requestHelper = pureModificationWatchpointProfile.newModificationWatchpointRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[ModificationWatchpointEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultModificationWatchpointEventInfoProfile _)
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

    describe("#tryGetOrCreateModificationWatchpointRequestWithData") {
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

        val actual = pureModificationWatchpointProfile.tryGetOrCreateModificationWatchpointRequest(
          className,
          fieldName,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }
    describe("#modificationWatchpointRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ModificationWatchpointRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someFieldName"
          )
        )

        val mockModificationWatchpointManager = mock[PendingModificationWatchpointSupportLike]
        val pureModificationWatchpointProfile = new Object with PureModificationWatchpointRequest {
          override protected val modificationWatchpointManager = mockModificationWatchpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()

        (mockModificationWatchpointManager.pendingModificationWatchpointRequests _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.modificationWatchpointRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ModificationWatchpointRequestInfo(
            TestRequestId,
            true,
            "some.class.name",
            "someFieldName"
          )
        )

        val mockModificationWatchpointManager = mock[PendingModificationWatchpointSupportLike]
        val pureModificationWatchpointProfile = new Object with PureModificationWatchpointRequest {
          override protected val modificationWatchpointManager = mockModificationWatchpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        (mockModificationWatchpointManager.pendingModificationWatchpointRequests _).expects()
          .returning(expected).once()

        val actual = pureModificationWatchpointProfile.modificationWatchpointRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ModificationWatchpointRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someFieldName"
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()

        val actual = pureModificationWatchpointProfile.modificationWatchpointRequests

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequests(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequests(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequests(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequests(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequests(
          className,
          fieldName
        )

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeModificationWatchpointRequestWithArgs(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllModificationWatchpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.removeAllModificationWatchpointRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeAllModificationWatchpointRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockModificationWatchpointManager.removeModificationWatchpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureModificationWatchpointProfile.removeAllModificationWatchpointRequests()

        actual should be (expected)
      }
    }

    describe("#isModificationWatchpointRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestPending(
          className,
          fieldName
        )

        actual should be (expected)
      }
    }

    describe("#isModificationWatchpointRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName + 1,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
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
          ModificationWatchpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            fieldName = fieldName,
            extraArguments = extraArguments
          )
        )

        (mockModificationWatchpointManager.modificationWatchpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureModificationWatchpointProfile.isModificationWatchpointRequestWithArgsPending(
          className,
          fieldName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
