package org.scaladebugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{ClassPrepareRequest, EventRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class StandardClassPrepareManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val classPrepareManager = new StandardClassPrepareManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardClassPrepareManager") {
    describe("#classPrepareRequestList") {
      it("should contain all class prepare requests in the form of id -> request stored in the manager") {
        val classPrepareRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a ClassPrepareManager whose generated request ids match the list
        // above
        val classPrepareManager = new StandardClassPrepareManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            classPrepareRequests(counter.getAndIncrement % classPrepareRequests.length)
          }
        }

        classPrepareRequests.foreach { case id =>
          (mockEventRequestManager.createClassPrepareRequest _).expects()
            .returning(stub[ClassPrepareRequest]).once()
          classPrepareManager.createClassPrepareRequest()
        }

        classPrepareManager.classPrepareRequestList should
          contain theSameElementsAs (classPrepareRequests)
      }
    }

    describe("#createClassPrepareRequestWithId") {
      it("should create the class prepare request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockClassPrepareRequest = mock[ClassPrepareRequest]
        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(mockClassPrepareRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockClassPrepareRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_ALL).once()
        (mockClassPrepareRequest.setEnabled _).expects(true).once()

        val actual = classPrepareManager.createClassPrepareRequestWithId(expected.get)
        actual should be(expected)
      }

      it("should create the class prepare request and return Success(id)") {
        val expected = Success(TestRequestId)

        val mockClassPrepareRequest = mock[ClassPrepareRequest]
        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(mockClassPrepareRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockClassPrepareRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_ALL).once()
        (mockClassPrepareRequest.setEnabled _).expects(true).once()

        val actual = classPrepareManager.createClassPrepareRequestWithId(
          expected.get
        )
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = classPrepareManager.createClassPrepareRequestWithId(
          TestRequestId
        )
        actual should be (expected)
      }
    }

    describe("#hasClassPrepareRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stub[ClassPrepareRequest]).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        val actual = classPrepareManager.hasClassPrepareRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = classPrepareManager.hasClassPrepareRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getClassPrepareRequest") {
      it("should return Some(ClassPrepareRequest) if found") {
        val expected = stub[ClassPrepareRequest]

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(expected).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        val actual = classPrepareManager.getClassPrepareRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequest(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getClassPrepareRequestInfo") {
      it("should return Some(info) if found") {
        val expected = ClassPrepareRequestInfo(
          TestRequestId,
          false,
          Seq(
            mock[JDIRequestArgument],
            mock[JDIRequestArgument]
          )
        )
        expected.extraArguments.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stub[ClassPrepareRequest]).once()

        val id = classPrepareManager.createClassPrepareRequest(
          expected.extraArguments: _*
        ).get

        val actual = classPrepareManager.getClassPrepareRequestInfo(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequestInfo(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeClassPrepareRequest") {
      it("should return true if the class prepare request was removed") {
        val expected = true
        val stubRequest = stub[ClassPrepareRequest]

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stubRequest).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = classPrepareManager.removeClassPrepareRequest(id)
        actual should be (expected)
      }

      it("should return false if the class prepare request was not removed") {
        val expected = false

        val actual = classPrepareManager.removeClassPrepareRequest(TestRequestId)
        actual should be (expected)
      }
    }
  }
}
