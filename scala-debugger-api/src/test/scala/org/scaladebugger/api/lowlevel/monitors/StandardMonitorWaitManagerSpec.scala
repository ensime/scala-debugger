package org.scaladebugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, MonitorWaitRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success}

class StandardMonitorWaitManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorWaitManager = new StandardMonitorWaitManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardMonitorWaitManager") {
    describe("#monitorWaitRequestList") {
      it("should contain all monitor wait requests in the form of id -> request stored in the manager") {
        val monitorWaitRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorWaitManager whose generated request ids match the list
        // above
        val monitorWaitManager = new StandardMonitorWaitManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorWaitRequests(counter.getAndIncrement % monitorWaitRequests.length)
          }
        }

        monitorWaitRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorWaitRequest _).expects()
            .returning(stub[MonitorWaitRequest]).once()
          monitorWaitManager.createMonitorWaitRequest()
        }

        monitorWaitManager.monitorWaitRequestList should
          contain theSameElementsAs (monitorWaitRequests)
      }
    }

    describe("#createMonitorWaitRequestWithId") {
      it("should create the monitor wait request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockMonitorWaitRequest = mock[MonitorWaitRequest]
        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(mockMonitorWaitRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorWaitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorWaitRequest.setEnabled _).expects(true).once()

        val actual =
          monitorWaitManager.createMonitorWaitRequestWithId(expected.get)
        actual should be(expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorWaitManager.createMonitorWaitRequestWithId(
          TestRequestId
        )
        actual should be (expected)
      }
    }

    describe("#hasMonitorWaitRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stub[MonitorWaitRequest]).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        val actual = monitorWaitManager.hasMonitorWaitRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorWaitManager.hasMonitorWaitRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitRequest") {
      it("should return Some(MonitorWaitRequest) if found") {
        val expected = stub[MonitorWaitRequest]

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(expected).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        val actual = monitorWaitManager.getMonitorWaitRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitManager.getMonitorWaitRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitRequestInfo") {
      it("should return Some(info) if found") {
        val expected = MonitorWaitRequestInfo(
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

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stub[MonitorWaitRequest]).once()

        val id = monitorWaitManager.createMonitorWaitRequest(
          expected.extraArguments: _*
        ).get

        val actual = monitorWaitManager.getMonitorWaitRequestInfo(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitManager.getMonitorWaitRequestInfo(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitRequest") {
      it("should return true if the monitor wait request was removed") {
        val expected = true
        val stubRequest = stub[MonitorWaitRequest]

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorWaitManager.removeMonitorWaitRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor wait request was not removed") {
        val expected = false

        val actual = monitorWaitManager.removeMonitorWaitRequest(TestRequestId)
        actual should be (expected)
      }
    }
  }
}
