package org.scaladebugger.api.profiles.pure.requests.vm

import com.sun.jdi.event.VMStartEvent
import org.scaladebugger.api.lowlevel.StandardRequestInfo
import org.scaladebugger.api.lowlevel.events.EventType.VMStartEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{EventInfoProducer, VMStartEventInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{JDIMockHelpers, TestRequestHelper}

import scala.util.Success

class PureVMStartRequestSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private type E = VMStartEvent
  private type EI = VMStartEventInfo
  private type EIData = (EI, Seq[JDIEventDataResult])
  private type RequestArgs = Seq[JDIRequestArgument]
  private type CounterKey = Seq[JDIRequestArgument]
  private class CustomTestRequestHelper extends TestRequestHelper[E, EI, RequestArgs, CounterKey](
    scalaVirtualMachine = mockScalaVirtualMachine,
    eventManager = mockEventManager,
    etInstance = VMStartEventType
  )

  private class TestPureVMStartRequest(
    private val customTestRequestHelper: Option[CustomTestRequestHelper] = None
  ) extends PureVMStartRequest {
    override def newVMStartRequestHelper() = {
      val originalRequestHelper = super.newVMStartRequestHelper()
      customTestRequestHelper.getOrElse(originalRequestHelper)
    }
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  private val mockRequestHelper = mock[CustomTestRequestHelper]
  private val pureVMStartProfile =
    new TestPureVMStartRequest(Some(mockRequestHelper))

  describe("PureVMStartRequest") {
    describe("for custom request helper") {
      describe("#_newRequestId") {
        it("should return a new id each time") {
          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()

          val requestId1 = requestHelper._newRequestId()
          val requestId2 = requestHelper._newRequestId()

          requestId1 shouldBe a [String]
          requestId2 shouldBe a [String]
          requestId1 should not be (requestId2)
        }
      }

      describe("#_hasRequest") {
        it("should return true if a request exists with the specified args") {
          val expected = true

          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])

          requestHelper._newRequest(requestId, requestArgs, requestArgs)

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }

        it("should return false if no request exists with the specified args") {
          val expected = false

          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()

          val requestArgs = Seq(mock[JDIRequestArgument])

          val actual = requestHelper._hasRequest(requestArgs)

          actual should be (expected)
        }
      }

      describe("#_removeByRequestId") {
        it("should remove the request with the specified id") {
          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()

          val requestId = "some id"
          val requestArgs = Seq(mock[JDIRequestArgument])

          requestHelper._newRequest(requestId, requestArgs, requestArgs)
          requestHelper._removeRequestById(requestId)
          requestHelper._hasRequest(requestArgs) should be (false)
        }
      }


      describe("#_retrieveRequestInfo") {
        it("should construct info for the request with the specified id") {
          val expected = Some(StandardRequestInfo(
            requestId = "some id",
            isPending = true,
            extraArguments = Seq(mock[JDIRequestArgument])
          ))

          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()


          val actual = expected.flatMap(info => {
            requestHelper._newRequest(
              info.requestId,
              info.extraArguments,
              info.extraArguments
            )
            requestHelper._retrieveRequestInfo(info.requestId)
          })

          actual should be (expected)
        }
      }

      describe("#_newEventInfo") {
        it("should create new event info for the specified args") {
          val expected = mock[VMStartEventInfo]

          val pureVMStartProfile = new TestPureVMStartRequest()
          val requestHelper = pureVMStartProfile.newVMStartRequestHelper()

          val mockEventProducer = mock[EventInfoProducer]
          (mockInfoProducer.eventProducer _).expects()
            .returning(mockEventProducer).once()

          val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
          val mockEvent = mock[VMStartEvent]
          val mockJdiArgs = Seq(mock[JDIRequestArgument], mock[JDIEventArgument])
          (mockEventProducer.newDefaultVMStartEventInfoProfile _)
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

    describe("#tryGetOrCreateVMStartRequestWithData") {
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

        val actual = pureVMStartProfile.tryGetOrCreateVMStartRequest(
          mockJdiRequestArgs ++ mockJdiEventArgs: _*
        ).get

        actual shouldBe an [IdentityPipeline[EIData]]
      }
    }
  }
}
