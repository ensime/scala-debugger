package org.scaladebugger.api.lowlevel.threads

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, ThreadDeathRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestArgumentProcessor, JDIRequestProcessor}
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success}

class StandardThreadDeathManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val threadDeathManager = new StandardThreadDeathManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardThreadDeathManager") {
    describe("#threadDeathRequestList") {
      it("should contain all thread death requests in the form of id -> request stored in the manager") {
        val threadDeathRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a ThreadDeathManager whose generated request ids match the list
        // above
        val threadDeathManager = new StandardThreadDeathManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            threadDeathRequests(counter.getAndIncrement % threadDeathRequests.length)
          }
        }

        threadDeathRequests.foreach { case id =>
          (mockEventRequestManager.createThreadDeathRequest _).expects()
            .returning(stub[ThreadDeathRequest]).once()
          threadDeathManager.createThreadDeathRequest()
        }

        threadDeathManager.threadDeathRequestList should
          contain theSameElementsAs (threadDeathRequests)
      }
    }

    describe("#createThreadDeathRequestWithId") {
      it("should create the thread death request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockThreadDeathRequest = mock[ThreadDeathRequest]
        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(mockThreadDeathRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockThreadDeathRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockThreadDeathRequest.setEnabled _).expects(true).once()

        val actual =
          threadDeathManager.createThreadDeathRequestWithId(expected.get)
        actual should be(expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = threadDeathManager.createThreadDeathRequestWithId(
          TestRequestId
        )
        actual should be (expected)
      }
    }

    describe("#hasThreadDeathRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(stub[ThreadDeathRequest]).once()

        val id = threadDeathManager.createThreadDeathRequest().get

        val actual = threadDeathManager.hasThreadDeathRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = threadDeathManager.hasThreadDeathRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getThreadDeathRequest") {
      it("should return Some(ThreadDeathRequest) if found") {
        val expected = stub[ThreadDeathRequest]

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(expected).once()

        val id = threadDeathManager.createThreadDeathRequest().get

        val actual = threadDeathManager.getThreadDeathRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = threadDeathManager.getThreadDeathRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getThreadDeathRequestInfo") {
      it("should return Some(info) if found") {
        val expected = ThreadDeathRequestInfo(
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

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(stub[ThreadDeathRequest]).once()

        val id = threadDeathManager.createThreadDeathRequest(
          expected.extraArguments: _*
        ).get

        val actual = threadDeathManager.getThreadDeathRequestInfo(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = threadDeathManager.getThreadDeathRequestInfo(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeThreadDeathRequest") {
      it("should return true if the thread death request was removed") {
        val expected = true
        val stubRequest = stub[ThreadDeathRequest]

        (mockEventRequestManager.createThreadDeathRequest _).expects()
          .returning(stubRequest).once()

        val id = threadDeathManager.createThreadDeathRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = threadDeathManager.removeThreadDeathRequest(id)
        actual should be (expected)
      }

      it("should return false if the thread death request was not removed") {
        val expected = false

        val actual = threadDeathManager.removeThreadDeathRequest(TestRequestId)
        actual should be (expected)
      }
    }
  }
}
