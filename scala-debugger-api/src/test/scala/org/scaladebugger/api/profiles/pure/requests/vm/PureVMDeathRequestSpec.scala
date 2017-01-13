package org.scaladebugger.api.profiles.pure.requests.vm
import com.sun.jdi.event.VMDeathEvent
import org.scaladebugger.api.lowlevel.events.EventType.VMDeathEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.vm.{PendingVMDeathSupportLike, VMDeathManager, VMDeathRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, VMDeathEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureVMDeathRequestSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockVMDeathManager = mock[VMDeathManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = VMDeathEvent
  private type EI = VMDeathEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = VMDeathEventType
  )

  private class TestPureVMDeathRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureVMDeathRequest {
    override def newVMDeathRequestHelper() = {
      val originalRequestHelper = super.newVMDeathRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val vmDeathManager = mockVMDeathManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureVMDeathProfile =
    new TestPureVMDeathRequest(Some(mockRequestHelper))

  describe("PureVMDeathRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

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

          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val requestId = expected.get
          val requestArgs = Seq(mock[JDIRequestArgument])
          val jdiRequestArgs = Seq(mock[JDIRequestArgument])

          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(requestId, jdiRequestArgs)
            .returning(expected)
            .once()

          val actual = requestHelper._newRequest(requestId, requestArgs, jdiRequestArgs)

          actual should be (expected)
        }
      }

      describe("#_hasRequest") {
        it("should return true if a request exists with matching request arguments") {
          val expected = true

          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = VMDeathRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = requestArgs
          )

          // Get a list of request ids
          (mockVMDeathManager.vmDeathRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that has arguments
          (mockVMDeathManager.getVMDeathRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }

        it("should return false if no request exists with matching request arguments") {
          val expected = false

          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])
          val requestInfo = VMDeathRequestInfo(
            requestId = requestId,
            isPending = false,
            extraArguments = Seq(mock[JDIRequestArgument])
          )

          // Get a list of request ids
          (mockVMDeathManager.vmDeathRequestList _).expects()
            .returning(Seq(requestId)).once()

          // Look up a request that does not have same arguments
          (mockVMDeathManager.getVMDeathRequestInfo _).expects(requestId)
            .returning(Some(requestInfo)).once()

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val requestId = "some id"

          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(requestId)
            .returning(true)
            .once()

          requestHelper._removeRequestById(requestId)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should get the info for the request with the specified id") {
          val expected = Some(VMDeathRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val requestId = "some id"

          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(requestId)
            .returning(expected)
            .once()

          val actual = requestHelper._retrieveRequestInfo(requestId)

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[VMDeathEventInfo]

          val pureVMDeathProfile = new TestPureVMDeathRequest()
          val requestHelper = pureVMDeathProfile.newVMDeathRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[VMDeathEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultVMDeathEventInfo _)
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

    describe("#tryGetOrCreateVMDeathRequestWithData") {
      it("should use the request helper's request and event pipeline methods") {
        val requestId = java.util.UUID.randomUUID().toString
        val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
        val mockJdiEventArgs = Seq(mock[JDIEventArgument])
        val requestArgs = mockJdiRequestArgs

        (mockRequestHelper.newRequest _)
          .expects(requestArgs, mockJdiRequestArgs)
          .returning(Success(requestId)).once()
        (mockRequestHelper.newEventPipeline _)
          .expects(requestId, mockJdiEventArgs, requestArgs)
          .returning(Success(Pipeline.newPipeline(classOf[EIData]))).once()

        val actual = pureVMDeathProfile.tryGetOrCreateVMDeathRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }

    describe("#vmDeathRequests") {
      it("should include all active requests") {
        val expected = Seq(
          VMDeathRequestInfo(TestRequestId, false)
        )

        val mockVMDeathManager = mock[PendingVMDeathSupportLike]
        val pureVMDeathProfile = new Object with PureVMDeathRequest {
          override protected val vmDeathManager = mockVMDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockVMDeathManager.getVMDeathRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockVMDeathManager.pendingVMDeathRequests _).expects()
          .returning(Nil).once()

        val actual = pureVMDeathProfile.vmDeathRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          VMDeathRequestInfo(TestRequestId, true)
        )

        val mockVMDeathManager = mock[PendingVMDeathSupportLike]
        val pureVMDeathProfile = new Object with PureVMDeathRequest {
          override protected val vmDeathManager = mockVMDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Nil).once()

        (mockVMDeathManager.pendingVMDeathRequests _).expects()
          .returning(expected).once()

        val actual = pureVMDeathProfile.vmDeathRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          VMDeathRequestInfo(TestRequestId, false)
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockVMDeathManager.getVMDeathRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureVMDeathProfile.vmDeathRequests

        actual should be (expected)
      }
    }

    describe("#removeVMDeathRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureVMDeathProfile.removeVMDeathRequestWithArgs()

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureVMDeathProfile.removeVMDeathRequestWithArgs()

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureVMDeathProfile.removeVMDeathRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureVMDeathProfile.removeVMDeathRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllVMDeathRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureVMDeathProfile.removeAllVMDeathRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureVMDeathProfile.removeAllVMDeathRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureVMDeathProfile.removeAllVMDeathRequests()

        actual should be (expected)
      }
    }

    describe("#isVMDeathRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(Nil).once()

        val actual = pureVMDeathProfile.isVMDeathRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureVMDeathProfile.isVMDeathRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureVMDeathProfile.isVMDeathRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          VMDeathRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockVMDeathManager.vmDeathRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureVMDeathProfile.isVMDeathRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
  }
}

