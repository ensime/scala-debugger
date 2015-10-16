package org.senkbeil.debugger.api.exceptions

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequest, EventRequestManager, ExceptionRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import scala.collection.JavaConverters._

class ExceptionManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val mockReferenceType = mock[ReferenceType]
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager).once()

  // Use same mock reference type for any request to retrieve classes by name
  (mockVirtualMachine.classesByName _).expects(*)
    .returning(Seq(mockReferenceType).toList.asJava).anyNumberOfTimes()

  private val exceptionManager = new ExceptionManager(mockVirtualMachine)

  describe("ExceptionManager") {
    describe("#setCatchallException") {
      it("should create the exception request and return true") {
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        exceptionManager.setCatchallException(
          testNotifyCaught,
          testNotifyUncaught
        ) should be (true)
      }

      it("should return false if failed to create the exception request") {
        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .throwing(new Throwable).once()

        exceptionManager.setCatchallException(
          testNotifyCaught,
          testNotifyUncaught
        ) should be (false)
      }
    }

    describe("#hasCatchallException") {
      it("should return true if the catchall has been set") {
        val expected = true

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.setCatchallException(true, true) should be (true)

        val actual = exceptionManager.hasCatchallException
        actual should be (expected)
      }

      it("should return false if the catchall has not been set") {
        val expected = false

        val actual = exceptionManager.hasCatchallException
        actual should be (expected)
      }
    }

    describe("#getCatchallException") {
      it("should return Some(ExceptionRequest) if the catchall has been set") {
        val expected = Some(stub[ExceptionRequest])

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(expected.get).once()

        exceptionManager.setCatchallException(true, true) should be (true)

        val actual = exceptionManager.getCatchallException
        actual should be (expected)
      }

      it("should return None if the catchall has not been set") {
        val expected = None

        val actual = exceptionManager.getCatchallException
        actual should be (expected)
      }
    }

    describe("#removeCatchallException") {
      it("should return true if the exception request was removed") {
        val expected = true

        val stubExceptionRequest = stub[ExceptionRequest]

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(stubExceptionRequest).once()

        exceptionManager.setCatchallException(true, true) should be (true)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubExceptionRequest).once()

        val actual = exceptionManager.removeCatchallException()
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val actual = exceptionManager.removeCatchallException()
        actual should be (expected)
      }
    }

    describe("#exceptionList") {
      it("should contain all exception requests in the form of (class, method) stored in the manager") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val exceptionRequests = Seq("class1", "class2")

        exceptionRequests.foreach { case exceptionName =>
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(stub[ExceptionRequest]).once()
          exceptionManager.setException(
            exceptionName,
            testNotifyCaught,
            testNotifyUncaught
          )
        }

        exceptionManager.exceptionList should
          contain theSameElementsAs (exceptionRequests)
      }
    }

    describe("#setException") {
      it("should create the exception request and return true") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        ) should be (true)
      }

      it("should return false if no class is found for the request") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        // Set up the virtual machine to not return any classes when asked
        val mockVirtualMachine = mock[VirtualMachine]
        (mockVirtualMachine.eventRequestManager _).expects()
          .returning(mockEventRequestManager).once()
        (mockVirtualMachine.classesByName _).expects(*)
          .returning(Seq[ReferenceType]().toList.asJava).once()

        val exceptionManager = new ExceptionManager(mockVirtualMachine)

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        ) should be (false)
      }

      it("should return false if failed to create the exception request") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .throwing(new Throwable).once()

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        ) should be (false)
      }
    }

    describe("#hasException") {
      it("should return true if it exists") {
        val expected = true

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.hasException(testExceptionName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.hasException(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#getException") {
      it("should return Some(Seq(ExceptionRequest)) if found") {
        val expected = Seq(stub[ExceptionRequest])

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(expected.head).once()

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.getException(testExceptionName).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val testExceptionName = "some exception name"

        val actual = exceptionManager.getException(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#removeException") {
      it("should return true if the exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.setException(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = exceptionManager.removeException(testExceptionName)
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.removeException(testExceptionName)
        actual should be (expected)
      }
    }
  }
}
