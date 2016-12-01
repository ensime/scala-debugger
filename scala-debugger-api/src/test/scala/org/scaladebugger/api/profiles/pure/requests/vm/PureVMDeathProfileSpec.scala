package org.scaladebugger.api.profiles.pure.requests.vm
import acyclic.file
import com.sun.jdi.event.{Event, VMDeathEvent}
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType.VMDeathEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.vm.{PendingVMDeathSupportLike, VMDeathManager, VMDeathRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.{Failure, Success}

class PureVMDeathProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockVMDeathManager = mock[VMDeathManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = VMDeathEvent
  private type EI = VMDeathEventInfoProfile
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = VMDeathEventType
  )

  private val mockRequestHelper = mock[CustomTestRequestHelper]

  private val pureVMDeathProfile = new Object with PureVMDeathProfile {
    override protected def newVMDeathRequestHelper() = mockRequestHelper
    override protected val vmDeathManager = mockVMDeathManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureVMDeathProfile") {
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
        val pureVMDeathProfile = new Object with PureVMDeathProfile {
          override protected val vmDeathManager = mockVMDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
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
        val pureVMDeathProfile = new Object with PureVMDeathProfile {
          override protected val vmDeathManager = mockVMDeathManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
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

