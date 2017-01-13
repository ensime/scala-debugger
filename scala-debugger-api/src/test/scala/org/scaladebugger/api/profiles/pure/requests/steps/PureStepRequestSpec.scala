package org.scaladebugger.api.profiles.pure.requests.steps
import com.sun.jdi.ThreadReference
import com.sun.jdi.event.{Event, StepEvent}
import org.scaladebugger.api.lowlevel.events.EventType.StepEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.ThreadFilter
import org.scaladebugger.api.lowlevel.steps.{PendingStepSupportLike, StepManager, StepRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.{EventInfo, EventInfoProducer, StepEventInfo}
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest.time.{Milliseconds, Span}
import test.{JDIMockHelpers, Workarounds}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PureStepRequestSpec extends ParallelMockFunSpec with JDIMockHelpers with Futures
  with ScalaFutures
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(3000, Milliseconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadInfoProfile = mock[ThreadInfo]
  private val mockStepManager = mock[StepManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducer]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureStepProfile = new Object with PureStepRequest {
    override protected val stepManager = mockStepManager
    override protected val eventManager = mockEventManager
    override protected val infoProducer: InfoProducer = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureStepRequest") {
    describe("#stepRequests") {
      it("should include all active requests") {
        val expected = Seq(
          StepRequestInfo(
            TestRequestId,
            false,
            false,
            mock[ThreadReference],
            0,
            1
          )
        )

        val mockStepManager = mock[PendingStepSupportLike]
        val pureStepProfile = new Object with PureStepRequest {
          override protected val stepManager = mockStepManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()

        (mockStepManager.pendingStepRequests _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.stepRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          StepRequestInfo(
            TestRequestId,
            true,
            false,
            mock[ThreadReference],
            0,
            1
          )
        )

        val mockStepManager = mock[PendingStepSupportLike]
        val pureStepProfile = new Object with PureStepRequest {
          override protected val stepManager = mockStepManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducer = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        (mockStepManager.pendingStepRequests _).expects()
          .returning(expected).once()

        val actual = pureStepProfile.stepRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          StepRequestInfo(
            TestRequestId,
            false,
            false,
            mock[ThreadReference],
            0,
            1
          )
        )

        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()

        val actual = pureStepProfile.stepRequests

        actual should be (expected)
      }
    }

    describe("#removeStepRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val threadInfoProfile = mock[ThreadInfo]

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.removeStepRequests(
          threadInfoProfile
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching thread exists") {
        val expected = Nil
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mock[ThreadReference],
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.removeStepRequests(
          mockThreadInfoProfile
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeStepRequests(
          mockThreadInfoProfile
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeStepRequests(
          mockThreadInfoProfile
        )

        actual should be (expected)
      }
    }

    describe("#removeStepRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val threadInfoProfile = mock[ThreadInfo]

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.removeStepRequestWithArgs(
          threadInfoProfile
        )

        actual should be (expected)
      }

      it("should return None if no request with matching thread exists") {
        val expected = None
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mock[ThreadReference],
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.removeStepRequestWithArgs(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.removeStepRequestWithArgs(
          mockThreadInfoProfile
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeStepRequestWithArgs(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val removeExistingRequests = true
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeStepRequestWithArgs(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllStepRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.removeAllStepRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val removeExistingRequests = true
        val threadInfoProfile = mock[ThreadInfo]
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeAllStepRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val removeExistingRequests = true
        val threadInfoProfile = mock[ThreadInfo]
        val size = 1
        val depth = 2
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            removeExistingRequests = removeExistingRequests,
            threadReference = mockThreadReference,
            size = size,
            depth = depth,
            extraArguments = extraArguments
          )
        )

        (mockStepManager.stepRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockStepManager.removeStepRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureStepProfile.removeAllStepRequests()

        actual should be (expected)
      }
    }

    describe("#isStepRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val threadInfoProfile = mock[ThreadInfo]

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.isStepRequestPending(threadInfoProfile)

        actual should be (expected)
      }

      it("should return false if no request with matching thread exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = false,
            threadReference = mock[ThreadReference],
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestPending(mockThreadInfoProfile)

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            removeExistingRequests = false,
            threadReference = mockThreadReference,
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestPending(mockThreadInfoProfile)

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = false,
            threadReference = mockThreadReference,
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestPending(mockThreadInfoProfile)

        actual should be (expected)
      }
    }

    describe("#isStepRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val threadInfoProfile = mock[ThreadInfo]
        val extraArguments = Seq(mock[JDIRequestArgument])

        (mockStepManager.stepRequestList _).expects()
          .returning(Nil).once()

        val actual = pureStepProfile.isStepRequestWithArgsPending(
          threadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching thread exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = false,
            threadReference = mock[ThreadReference],
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestWithArgsPending(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = false,
            threadReference = mockThreadReference,
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestWithArgsPending(
          mockThreadInfoProfile
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            removeExistingRequests = false,
            threadReference = mockThreadReference,
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestWithArgsPending(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          StepRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            removeExistingRequests = false,
            threadReference = mockThreadReference,
            size = 0,
            depth = 0,
            extraArguments = extraArguments
          )
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()
        (mockStepManager.stepRequestList _).expects()
          .returning(requests).once()

        val actual = pureStepProfile.isStepRequestWithArgsPending(
          mockThreadInfoProfile,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
    
    describe("#stepIntoLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepIntoLineRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepIntoLineWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepIntoLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepIntoLineWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOutLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val stepPipeline = Pipeline.newPipeline(
          classOf[(EventInfo, Seq[JDIEventDataResult])]
        )
        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOutLineRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepOutLineWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOutLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOutLineWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOverLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOverLineRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepOverLineWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOverLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOverLineWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#stepIntoMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepIntoMinRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepIntoMinWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepIntoMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepIntoMinWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOutMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOutMinRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepOutMinWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOutMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOutMinWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOverMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val mockStepEvent = mock[StepEvent]

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs :+ threadFilter
        )

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOverMinRequest _)
          .expects(mockThreadReference, stepRequestInfo.extraArguments)
          .returning(Success(TestRequestId)).once()

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(lowlevelPipeline).once()

        val stepFuture = pureStepProfile.stepOverMinWithData(
          mockThreadInfoProfile,
          rArgs ++ eArgs: _*
        )

        // Should convert event to info representation
        val mockEventInfoProducer = mock[EventInfoProducer]
        (mockInfoProducer.eventProducer _).expects()
          .returning(mockEventInfoProducer).once()
        (mockEventInfoProducer.newDefaultStepEventInfo _).expects(
          mockScalaVirtualMachine,
          mockStepEvent,
          stepRequestInfo.extraArguments ++ eArgs
        ).returning(expected._1).once()

        // Process the pipeline to trigger the future
        lowlevelPipeline.process((mockStepEvent, expected._2))

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture steps thrown when creating the request") {
        val expected = new Throwable

        (mockThreadInfoProfile.toJdiInstance _).expects()
          .returning(mockThreadReference).once()

        (mockStepManager.createStepOverMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOverMinWithData(
          mockThreadInfoProfile
        )

        val stepFutureFailed = Workarounds.transformFutureToFailed(stepFuture)

        whenReady(stepFutureFailed) { actual => actual should be (expected) }
      }
    }

    describe("#tryCreateStepListenerWithData") {
      it("should create a stream of events with data for steps") {
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])
        val arguments = rArgs ++ eArgs

        val stepRequestInfo = StepRequestInfo(
          requestId = "",
          isPending = true,
          removeExistingRequests = true,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 0,
          extraArguments = rArgs
        )

        (mockStepManager.getStepRequestInfoWithId _).expects(*)
          .returning(Some(stepRequestInfo)).once()

        val lowlevelPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        (mockEventManager.addEventDataStream _).expects(
          StepEventType, eArgs
        ).returning(lowlevelPipeline).once()

        var actual: (StepEventInfo, Seq[JDIEventDataResult]) = null
        val pipeline = pureStepProfile.tryCreateStepListenerWithData(
          mockThreadInfoProfile, arguments: _*
        )
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
