package org.scaladebugger.api.profiles.pure.exceptions
import acyclic.file

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.ExceptionEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.exceptions.{ExceptionManager, ExceptionRequestInfo, PendingExceptionSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.Constants
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureExceptionProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockExceptionManager = mock[ExceptionManager]
  private val mockEventManager = mock[EventManager]

  private val pureExceptionProfile = new Object with PureExceptionProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newExceptionRequestId(): String =
      if (requestId != null) requestId else super.newExceptionRequestId()

    override protected val exceptionManager = mockExceptionManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureExceptionProfile") {
    describe("#exceptionRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, false, "some.exception.class", true, false)
        )

        val mockExceptionManager = mock[PendingExceptionSupportLike]
        val pureExceptionProfile = new Object with PureExceptionProfile {
          override protected val exceptionManager = mockExceptionManager
          override protected val eventManager: EventManager = mockEventManager
        }

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()

        (mockExceptionManager.pendingExceptionRequests _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.exceptionRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, true, "some.exception.class", true, false)
        )

        val mockExceptionManager = mock[PendingExceptionSupportLike]
        val pureExceptionProfile = new Object with PureExceptionProfile {
          override protected val exceptionManager = mockExceptionManager
          override protected val eventManager: EventManager = mockEventManager
        }

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        (mockExceptionManager.pendingExceptionRequests _).expects()
          .returning(expected).once()

        val actual = pureExceptionProfile.exceptionRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, false, "some.exception.class", true, false)
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()

        val actual = pureExceptionProfile.exceptionRequests

        actual should be (expected)
      }
    }

    describe("#removeOnlyAllExceptionsRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequests()

        actual should be (expected)
      }

      it("should return empty if no catchall exception request exists") {
        val expected = Nil
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequests()

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequests()

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequests()

        actual should be (expected)
      }
    }

    describe("#removeOnlyAllExceptionsRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val notifyCaught = true
        val notifyUncaught = false

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return None if no catchall exception request exists") {
        val expected = None
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching notify caught flag exists") {
        val expected = None
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = !notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching notify uncaught flag exists") {
        val expected = None
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = !notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = ExceptionRequestInfo.DefaultCatchallExceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val exceptionName = "some.exception.name"

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.removeExceptionRequests(
          exceptionName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching exception name exists") {
        val expected = Nil
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeExceptionRequests(
          exceptionName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeExceptionRequests(
          exceptionName
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeExceptionRequests(
          exceptionName
        )

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return None if no request with matching exception name exists") {
        val expected = None
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching notify caught flag exists") {
        val expected = None
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = !notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching notify uncaught flag exists") {
        val expected = None
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = !notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeExceptionRequestWithArgs(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllExceptionRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.removeAllExceptionRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeAllExceptionRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureExceptionProfile.removeAllExceptionRequests()

        actual should be (expected)
      }
    }

    describe("#isAllExceptionsRequestPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }

      it("should return false if no request with matching \"all exceptions\" name exists") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }
    }

    describe("#isAllExceptionsRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching exception name exists") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching notify caught exists") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = !notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching notify uncaught exists") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = !notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val exceptionName = ExceptionRequestInfo.DefaultCatchallExceptionName
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#isExceptionRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val exceptionName = "some.exception.name"

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching exception name exists") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }
    }

    describe("#isExceptionRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(Nil).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching exception name exists") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName + "other",
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching notify caught exists") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = !notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching notify uncaught exists") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = !notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          ExceptionRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = exceptionName,
            notifyCaught = notifyCaught,
            notifyUncaught = notifyUncaught,
            extraArguments = extraArguments
          )
        )

        (mockExceptionManager.exceptionRequestList _).expects()
          .returning(requests).once()

        val actual = pureExceptionProfile.isExceptionRequestWithArgsPending(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#tryGetOrCreateExceptionRequestWithData") {
      it("should create a new request if one has not be made yet") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return false this time to indicate that the exception request
          // was removed some time between the two calls
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId + "other",
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return true to indicate that we do still have the request
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should create a new request for different input") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName + 1)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId + "other",
            exceptionName + 1,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName + 1,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(false).once()
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createExceptionRequestWithId _).expects(
              TestRequestId,
              exceptionName,
              notifyCaught,
              notifyUncaught,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }

      it("should remove the underlying request if close data says to do so") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(false).once()
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createExceptionRequestWithId _).expects(
              TestRequestId,
              exceptionName,
              notifyCaught,
              notifyUncaught,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close(now = true, data = Constants.CloseRemoveAll))
      }
    }

    describe("#tryGetOrCreateAllExceptionsRequestWithData") {
      it("should create a new request if one has not be made yet") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the exception request
          // was removed some time between the two calls
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId + "other",
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return matching info to indicate that we do still have the request
          (mockExceptionManager.exceptionRequestList _).expects()
            .returning(Seq(ExceptionRequestInfo(
              requestId = TestRequestId,
              false,
              className = ExceptionRequestInfo.DefaultCatchallExceptionName,
              notifyCaught = notifyCaught,
              notifyUncaught = notifyUncaught,
              extraArguments = arguments
            ))).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Nil).once()
            // Return matching info to indicate that we do still have the request
            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Seq(ExceptionRequestInfo(
                requestId = TestRequestId,
                false,
                className = ExceptionRequestInfo.DefaultCatchallExceptionName,
                notifyCaught = notifyCaught,
                notifyUncaught = notifyUncaught,
                extraArguments = uniqueIdProperty +: arguments
              ))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createCatchallExceptionRequestWithId _)
              .expects(
                TestRequestId,
                notifyCaught,
                notifyUncaught,
                uniqueIdProperty +: arguments
              )
              .returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }

      it("should remove the underlying request if close data says to do so") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Nil).once()
            // Return matching info to indicate that we do still have the request
            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Seq(ExceptionRequestInfo(
                requestId = TestRequestId,
                false,
                className = ExceptionRequestInfo.DefaultCatchallExceptionName,
                notifyCaught = notifyCaught,
                notifyUncaught = notifyUncaught,
                extraArguments = uniqueIdProperty +: arguments
              ))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createCatchallExceptionRequestWithId _)
              .expects(
                TestRequestId,
                notifyCaught,
                notifyUncaught,
                uniqueIdProperty +: arguments
              )
              .returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close(now = true, data = Constants.CloseRemoveAll))
      }
    }
  }
}
