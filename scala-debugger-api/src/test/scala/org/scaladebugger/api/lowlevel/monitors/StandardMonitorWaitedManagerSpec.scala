package org.scaladebugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, MonitorWaitedRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success}

class StandardMonitorWaitedManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorWaitedManager = new StandardMonitorWaitedManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardMonitorWaitedManager") {
    describe("#monitorWaitedRequestList") {
      it("should contain all monitor waited requests in the form of id -> request stored in the manager") {
        val monitorWaitedRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorWaitedManager whose generated request ids match the list
        // above
        val monitorWaitedManager = new StandardMonitorWaitedManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorWaitedRequests(counter.getAndIncrement % monitorWaitedRequests.length)
          }
        }

        monitorWaitedRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorWaitedRequest _).expects()
            .returning(stub[MonitorWaitedRequest]).once()
          monitorWaitedManager.createMonitorWaitedRequest()
        }

        monitorWaitedManager.monitorWaitedRequestList should
          contain theSameElementsAs (monitorWaitedRequests)
      }
    }

    describe("#createMonitorWaitedRequestWithId") {
      it("should create the monitor waited request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]
        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(mockMonitorWaitedRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorWaitedRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorWaitedRequest.setEnabled _).expects(true).once()

        val actual =
          monitorWaitedManager.createMonitorWaitedRequestWithId(expected.get)
        actual should be(expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorWaitedManager.createMonitorWaitedRequestWithId(
          TestRequestId
        )
        actual should be (expected)
      }
    }

    describe("#hasMonitorWaitedRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stub[MonitorWaitedRequest]).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        val actual = monitorWaitedManager.hasMonitorWaitedRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorWaitedManager.hasMonitorWaitedRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedRequest") {
      it("should return Some(MonitorWaitedRequest) if found") {
        val expected = stub[MonitorWaitedRequest]

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(expected).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        val actual = monitorWaitedManager.getMonitorWaitedRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedRequestInfo") {
      it("should return Some(info) if found") {
        val expected = MonitorWaitedRequestInfo(
          TestRequestId,
          false,
          Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        )
        expected.extraArguments.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stub[MonitorWaitedRequest]).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest(
          expected.extraArguments: _*
        ).get

        val actual = monitorWaitedManager.getMonitorWaitedRequestInfo(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequestInfo(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitedRequest") {
      it("should return true if the monitor waited request was removed") {
        val expected = true
        val stubRequest = stub[MonitorWaitedRequest]

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorWaitedManager.removeMonitorWaitedRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor waited request was not removed") {
        val expected = false

        val actual = monitorWaitedManager.removeMonitorWaitedRequest(TestRequestId)
        actual should be (expected)
      }
    }
  }
}
