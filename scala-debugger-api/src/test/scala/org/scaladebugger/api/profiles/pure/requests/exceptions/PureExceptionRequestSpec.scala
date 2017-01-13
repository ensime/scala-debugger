package org.scaladebugger.api.profiles.pure.requests.exceptions
import com.sun.jdi.event.{Event, ExceptionEvent}
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.ExceptionEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.exceptions.{ExceptionManager, ExceptionRequestInfo, PendingExceptionSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, ExceptionEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureExceptionRequestSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockExceptionManager = mock[ExceptionManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = ExceptionEvent
  private type EI = ExceptionEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = (String, Boolean, Boolean, Seq[JDIRequestArgument])
  private type CounterKey = (String, Boolean, Boolean, Seq[JDIRequestArgument])
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = ExceptionEventType
  )

  private class TestPureExceptionRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureExceptionRequest {
    override def newExceptionRequestHelper(forCatchall: Boolean) = {
      val originalRequestHelper = super.newExceptionRequestHelper(forCatchall)
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val exceptionManager = mockExceptionManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureExceptionProfile =
    new TestPureExceptionRequest(Some(mockRequestHelper))

  describe("PureExceptionRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        describe("forCatchall == false") {
          it("should return a new id each time") {
            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val requestId1 = requestHelper._newRequestId()
            val requestId2 = requestHelper._newRequestId()

            requestId1 shouldBe a [String]
            requestId2 shouldBe a [String]
            requestId1 should not be (requestId2)
          }
        }

        describe("forCatchall == true") {
          it("should return a new id each time") {
            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val requestId1 = requestHelper._newRequestId()
            val requestId2 = requestHelper._newRequestId()

            requestId1 shouldBe a [String]
            requestId2 shouldBe a [String]
            requestId1 should not be (requestId2)
          }
        }
      }

      describe("#_newRequest") {
        describe("forCatchall == false") {
          it("should create a new request with the provided args and id") {
            val expected = Success("some id")

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val requestId = expected.get
            val exceptionName = "exception.name"
            val notifyCaught = true
            val notifyUncaught = false
            val requestArgs = (exceptionName, notifyCaught, notifyUncaught, Seq(mock[JDIRequestArgument]))
            val jdiRequestArgs = Seq(mock[JDIRequestArgument])

            (mockExceptionManager.createExceptionRequestWithId _).expects(
              requestId,
              exceptionName,
              notifyCaught,
              notifyUncaught,
              jdiRequestArgs
            ).returning(expected).once()

            val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

            actual should be (expected)
          }
        }

        describe("forCatchall == true") {
          it("should create a new request with the provided args and id") {
            val expected = Success("some id")

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val requestId = expected.get
            val notifyCaught = true
            val notifyUncaught = false
            val requestArgs = ("", notifyCaught, notifyUncaught, Seq(mock[JDIRequestArgument]))
            val jdiRequestArgs = Seq(mock[JDIRequestArgument])

            (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
              requestId,
              notifyCaught,
              notifyUncaught,
              jdiRequestArgs
            ).returning(expected).once()

            val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

            actual should be (expected)
          }
        }
      }

      describe("#_hasRequest") {
        describe("forCatchall == false") {
          it("should return the result of checking if a request with matching properties exists") {
            val expected = true

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val exceptionName = "exception.name"
            val notifyCaught = true
            val notifyUncaught = false
            val requestArgs = (exceptionName, notifyCaught, notifyUncaught, Seq(mock[JDIRequestArgument]))

            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(expected).once()

            val actual = requestHelper._hasRequest(requestArgs)

            actual should be(expected)
          }
        }

        describe("forCatchall == true") {
          it("should return false if a catchall exception with matching input does not exist") {
            val expected = false

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val exceptionRequestInfo = ExceptionRequestInfo(
              requestId = "some id",
              isPending = true,
              className = "some.class", // Not catchall
              notifyCaught = true,
              notifyUncaught = false,
              extraArguments = Seq(mock[JDIRequestArgument])
            )

            val requestArgs = (
              "",
              exceptionRequestInfo.notifyCaught,
              exceptionRequestInfo.notifyUncaught,
              exceptionRequestInfo.extraArguments
            )

            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Seq(exceptionRequestInfo)).once()

            val actual = requestHelper._hasRequest(requestArgs)

            actual should be(expected)
          }

          it("should return true if a catchall exception with matching input exists") {
            val expected = true

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val exceptionRequestInfo = ExceptionRequestInfo(
              requestId = "some id",
              isPending = true,
              className = ExceptionRequestInfo.DefaultCatchallExceptionName,
              notifyCaught = true,
              notifyUncaught = false,
              extraArguments = Seq(mock[JDIRequestArgument])
            )

            val requestArgs = (
              exceptionRequestInfo.className,
              exceptionRequestInfo.notifyCaught,
              exceptionRequestInfo.notifyUncaught,
              exceptionRequestInfo.extraArguments
            )

            (mockExceptionManager.exceptionRequestList _).expects()
              .returning(Seq(exceptionRequestInfo)).once()

            val actual = requestHelper._hasRequest(requestArgs)

            actual should be(expected)
          }
        }
      }

      describe("#_removeByRequestId") {
        describe("forCatchall == false") {
          it("should remove the request with the specified id") {
            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val requestId = "some id"

            (mockExceptionManager.removeExceptionRequestWithId _)
              .expects(requestId)
              .returning(true)
              .once()

            requestHelper._removeRequestById(requestId)
          }
        }

        describe("forCatchall == true") {
          it("should remove the request with the specified id") {
            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val requestId = "some id"

            (mockExceptionManager.removeExceptionRequestWithId _)
              .expects(requestId)
              .returning(true)
              .once()

            requestHelper._removeRequestById(requestId)
          }
        }
      }

      describe("#_retrieveRequestInfo") {
        describe("forCatchall == false") {
          it("should get the info for the request with the specified id") {
            val expected = Some(ExceptionRequestInfo(
              requestId = "some id",
              isPending = true,
              className = "some.name",
              notifyCaught = true,
              notifyUncaught = false,
              extraArguments = Seq(mock[JDIRequestArgument])
            ))

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val requestId = "some id"

            (mockExceptionManager.getExceptionRequestInfoWithId _)
              .expects(requestId)
              .returning(expected)
              .once()

            val actual = requestHelper._retrieveRequestInfo(requestId)

            actual should be (expected)
          }
        }

        describe("forCatchall == true") {
          it("should get the info for the request with the specified id") {
            val expected = Some(ExceptionRequestInfo(
              requestId = "some id",
              isPending = true,
              className = "some.name",
              notifyCaught = true,
              notifyUncaught = false,
              extraArguments = Seq(mock[JDIRequestArgument])
            ))

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val requestId = "some id"

            (mockExceptionManager.getExceptionRequestInfoWithId _)
              .expects(requestId)
              .returning(expected)
              .once()

            val actual = requestHelper._retrieveRequestInfo(requestId)

            actual should be (expected)
          }
        }
      }

      describe("#_newEventInfo") {
        describe("forCatchall == false") {
          it("should create new event info for the specified args") {
            val expected = mock[ExceptionEventInfo]

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = false
            )

            val mockEventProducer = mock[EventInfoProducer]
            (mockInfoProducer.eventProducer _).expects()
              .returning(mockEventProducer).once()

            val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
            val mockEvent = mock[ExceptionEvent]
            val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
            (mockEventProducer.newDefaultExceptionEventInfo _)
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

        describe("forCatchall == true") {
          it("should create new event info for the specified args") {
            val expected = mock[ExceptionEventInfo]

            val pureExceptionProfile = new TestPureExceptionRequest()
            val requestHelper = pureExceptionProfile.newExceptionRequestHelper(
              forCatchall = true
            )

            val mockEventProducer = mock[EventInfoProducer]
            (mockInfoProducer.eventProducer _).expects()
              .returning(mockEventProducer).once()

            val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
            val mockEvent = mock[ExceptionEvent]
            val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
            (mockEventProducer.newDefaultExceptionEventInfo _)
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
    }

    describe("#tryGetOrCreateExceptionRequestWithData") {
      it("should throw an exception if the exception name is null") {
        pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          null,
          notifyCaught = true,
          notifyUncaught = false,
          Seq(mock[JDIRequestArgument], mock[JDIEventArgument]): _*
        ).failed.get shouldBe an [IllegalArgumentException]
      }

      it("should use the standard request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val exceptionName = "some.name"
        val notifyCaught = true
        val notifyUncaught = false
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = (exceptionName, notifyCaught, notifyUncaught, mockJdiRequestArgs)

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureExceptionProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#tryGetOrCreateAllExceptionsRequestWithData") {
      it("should use the catchall request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val notifyCaught = true
        val notifyUncaught = false
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = (
          ExceptionRequestInfo.DefaultCatchallExceptionName,
          notifyCaught,
          notifyUncaught,
          mockJdiRequestArgs
        )

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#exceptionRequests") {
      it("should include all active requests") {
        val expected = Seq(
          ExceptionRequestInfo(TestRequestId, false, "some.exception.class", true, false)
        )

        val mockExceptionManager = mock[PendingExceptionSupportLike]
        val pureExceptionProfile = new Object with PureExceptionRequest {
          override protected val exceptionManager = mockExceptionManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
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
        val pureExceptionProfile = new Object with PureExceptionRequest {
          override protected val exceptionManager = mockExceptionManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
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
  }
}
