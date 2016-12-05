package org.scaladebugger.api.lowlevel.exceptions

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequest, EventRequestManager, ExceptionRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

class StandardExceptionManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockReferenceType = mock[ReferenceType]
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]

  // Use same mock reference type for any request to retrieve classes by name
  (mockVirtualMachine.classesByName _).expects(*)
    .returning(Seq(mockReferenceType).toList.asJava).anyNumberOfTimes()

  private val exceptionManager = new StandardExceptionManager(
    mockVirtualMachine,
    mockEventRequestManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardExceptionManager") {
    describe("#createCatchallExceptionRequestWithId") {
      it("should create the exception request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createCatchallExceptionRequestWithId(
          expected.get,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be(expected)
      }

      it("should add the exception request id to the standard exception request list") {
        val expected = java.util.UUID.randomUUID().toString
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        exceptionManager.createCatchallExceptionRequestWithId(
          expected,
          testNotifyCaught,
          testNotifyUncaught
        )

        exceptionManager.exceptionRequestListById should have length 1

        val actual = exceptionManager.exceptionRequestListById.head
        actual should be(expected)
      }

      it("should create the exception request and return Success(id)") {
        val expected = Success(TestRequestId)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createCatchallExceptionRequestWithId(
          expected.get,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the exception request") {
        val expected = Failure(new Throwable)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .throwing(expected.failed.get).once()

        val actual = exceptionManager.createCatchallExceptionRequestWithId(
          TestRequestId,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }
    }

    describe("#exceptionRequestListById") {
      it("should contain all exception requests in the form of (class, method) stored in the manager") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val exceptionRequests = Seq(("id1", "class1"), ("id2", "class2"))

        exceptionRequests.foreach { case (requestId, exceptionName) =>
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(stub[ExceptionRequest]).once()
          exceptionManager.createExceptionRequestWithId(
            requestId,
            exceptionName,
            testNotifyCaught,
            testNotifyUncaught
          )
        }

        exceptionManager.exceptionRequestListById should
          contain theSameElementsAs (exceptionRequests.map(_._1))
      }
    }

    describe("#exceptionRequestList") {
      it("should contain all exception requests in the form of (class, method) stored in the manager") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val exceptionRequests = Seq(
          ExceptionRequestInfo(TestRequestId, false, "class1", testNotifyCaught, testNotifyUncaught),
          ExceptionRequestInfo(TestRequestId + 1, false, "class2", testNotifyCaught, testNotifyUncaught)
        )

        // NOTE: Must create a new exception manager that does NOT override the
        //       request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val exceptionManager = new StandardExceptionManager(
          mockVirtualMachine,
          mockEventRequestManager
        )

        exceptionRequests.foreach { case exceptionInfo =>
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(stub[ExceptionRequest]).once()
          exceptionManager.createExceptionRequestWithId(
            exceptionInfo.requestId,
            exceptionInfo.className,
            exceptionInfo.notifyCaught,
            exceptionInfo.notifyUncaught
          )
        }

        exceptionManager.exceptionRequestList should
          contain theSameElementsAs (exceptionRequests)
      }
    }

    describe("#createExceptionRequestWithId") {
      it("should throw an exception if the exception name is null") {
        intercept[IllegalArgumentException] {
          exceptionManager.createExceptionRequestWithId("id", null, true, false)
        }
      }

      it("should create the exception request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testExceptionName = "some.exception.name"
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createExceptionRequestWithId(
          expected.get,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be(expected)
      }

      it("should return a custom exception if no class is found for the request") {
        val testExceptionName = "some.exception.name"
        val expected = Failure(NoExceptionClassFound(testExceptionName))
        val testNotifyCaught = true
        val testNotifyUncaught = false

        // Set up the virtual machine to not return any classes when asked
        val mockVirtualMachine = mock[VirtualMachine]
        (mockVirtualMachine.classesByName _).expects(*)
          .returning(Seq[ReferenceType]().toList.asJava).once()

        val exceptionManager = new StandardExceptionManager(
          mockVirtualMachine,
          mockEventRequestManager
        )

        val actual = exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the exception request") {
        val expected = Failure(new Throwable)
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .throwing(expected.failed.get).once()

        val actual = exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }
    }

    describe("#hasExceptionRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.hasExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = exceptionManager.hasExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasExceptionRequest") {
      it("should return true if it exists") {
        val expected = true

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.hasExceptionRequest(testExceptionName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.hasExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#getExceptionRequestInfoWithId") {
      it("should return Some(ExceptionRequestInfo(id, not pending, class name, notify caught, notify uncaught)) if the id exists") {
        val expected = Some(ExceptionRequestInfo(
          requestId = TestRequestId,
          isPending = false,
          className = "some.exception.name",
          notifyCaught = true,
          notifyUncaught = false
        ))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createExceptionRequest _).expects(*, *, *)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createExceptionRequestWithId(
          expected.get.requestId,
          expected.get.className,
          expected.get.notifyCaught,
          expected.get.notifyUncaught
        )

        val actual = exceptionManager.getExceptionRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }

      it("should return None if there is no breakpoint with the id") {
        val expected = None

        val actual = exceptionManager.getExceptionRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#getExceptionRequestWithId") {
      it("should return Some(Seq(ExceptionRequest)) if found") {
        val expected = Seq(stub[ExceptionRequest])

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(expected.head).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.getExceptionRequestWithId(TestRequestId).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = exceptionManager.getExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getExceptionRequest") {
      it("should return Some(Seq(ExceptionRequest)) if found") {
        val expected = Seq(stub[ExceptionRequest])

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(expected.head).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.getExceptionRequest(testExceptionName).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val testExceptionName = "some exception name"

        val actual = exceptionManager.getExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#removeExceptionRequestWithId") {
      it("should return true if the catchall exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createCatchallExceptionRequestWithId(
          TestRequestId,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequests _)
          .expects(Seq(stubRequest).asJava).once()

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return true if the exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequests _)
          .expects(Seq(stubRequest).asJava).once()

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeExceptionRequest") {
      it("should return true if the exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequests _)
          .expects(Seq(stubRequest).asJava).once()

        val actual = exceptionManager.removeExceptionRequest(testExceptionName)
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.removeExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }
  }
}
