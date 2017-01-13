package org.scaladebugger.api.profiles.pure.requests.breakpoints
import com.sun.jdi.event.{BreakpointEvent, Event}
import org.scaladebugger.api.lowlevel.breakpoints.{BreakpointManager, BreakpointRequestInfo, PendingBreakpointSupportLike}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.BreakpointEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{BreakpointEventInfo, EventInfoProducer}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureBreakpointRequestSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val stubClassManager = stub[ClassManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockBreakpointManager = mock[BreakpointManager]
  private val mockEventManager = mock[EventManager]

  private type E = BreakpointEvent
  private type EI = BreakpointEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, Int, Seq[JDIRequestArgument])
  private type CounterKey = (String, Int, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = BreakpointEventType
  )

  private class TestPureBreakpointRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureBreakpointRequest {
    override def newBreakpointRequestHelper() = {
      val originalRequestHelper = super.newBreakpointRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val breakpointManager = mockBreakpointManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureBreakpointProfile =
    new TestPureBreakpointRequest(Some(mockRequestHelper))

  describe("PureBreakpointRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

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

          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

          val requestId = expected.get
          val fileName = "class.name"
          val lineNumber = 999
          val requestArgs = (fileName, lineNumber, Seq(mock[JDIRequestArgument]))
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockBreakpointManager.createBreakpointRequestWithId _)
            .expects(requestId, fileName, lineNumber, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be (expected)
        }
      }

      describe("#_hasRequest") {
        it("should return the result of checking if a request with matching properties exists") {
          val expected = true

          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

          val fileName = "class.name"
          val lineNumber = 999
          val requestArgs = (fileName, lineNumber, Seq(mock[JDIRequestArgument]))

          (mockBreakpointManager.hasBreakpointRequest _)
            .expects(fileName, lineNumber)
            .returning(expected)
            .once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

          val requestId = "some id"

          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(BreakpointRequestInfo(
            requestId = "some id",
            isPending = true,
            fileName = "some.name",
            lineNumber = 999,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

          val requestId = "some id"

          (mockBreakpointManager.getBreakpointRequestInfoWithId _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[BreakpointEventInfo]

          val pureBreakpointProfile = new TestPureBreakpointRequest()
          val requestHelper = pureBreakpointProfile.newBreakpointRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[BreakpointEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultBreakpointEventInfo _)
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

    describe("#tryGetOrCreateBreakpointRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val fileName = "some.name"
        val lineNumber = 999
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = (fileName, lineNumber, mockJdiRequestArgs)

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureBreakpointProfile.tryGetOrCreateBreakpointRequest(
          fileName,
          lineNumber,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#breakpointRequests") {
      it("should include all active requests") {
        val expected = Seq(
          BreakpointRequestInfo(TestRequestId, false, "some file", 999)
        )

        val mockBreakpointManager = mock[PendingBreakpointSupportLike]
        val pureBreakpointProfile = new Object with PureBreakpointRequest {
          override protected val breakpointManager = mockBreakpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()

        (mockBreakpointManager.pendingBreakpointRequests _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.breakpointRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          BreakpointRequestInfo(TestRequestId, true, "some file", 999)
        )

        val mockBreakpointManager = mock[PendingBreakpointSupportLike]
        val pureBreakpointProfile = new Object with PureBreakpointRequest {
          override protected val breakpointManager = mockBreakpointManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        (mockBreakpointManager.pendingBreakpointRequests _).expects()
          .returning(expected).once()

        val actual = pureBreakpointProfile.breakpointRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          BreakpointRequestInfo(TestRequestId, false, "some file", 999)
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()

        val actual = pureBreakpointProfile.breakpointRequests

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.removeBreakpointRequests(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching filename exists") {
        val expected = Nil
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName + "other",
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.removeBreakpointRequests(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching line number exists") {
        val expected = Nil
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber + 1,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.removeBreakpointRequests(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeBreakpointRequests(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeBreakpointRequests(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return None if no request with matching filename exists") {
        val expected = None
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName + "other",
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching line number exists") {
        val expected = None
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber + 1,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeBreakpointRequestWithArgs(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllBreakpointRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.removeAllBreakpointRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeAllBreakpointRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockBreakpointManager.removeBreakpointRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureBreakpointProfile.removeAllBreakpointRequests()

        actual should be (expected)
      }
    }

    describe("#isBreakpointRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.isBreakpointRequestPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no request with matching filename exists") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName + "other",
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no request with matching line number exists") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber + 1,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }
    }

    describe("#isBreakpointRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(Nil).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no request with matching filename exists") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName + "other",
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no request with matching line number exists") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber + 1,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          BreakpointRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            fileName = fileName,
            lineNumber = lineNumber,
            extraArguments = extraArguments
          )
        )

        (mockBreakpointManager.breakpointRequestList _).expects()
          .returning(requests).once()

        val actual = pureBreakpointProfile.isBreakpointRequestWithArgsPending(
          fileName,
          lineNumber,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
