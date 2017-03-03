package org.scaladebugger.api.profiles

import _root_.java.util.UUID

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.{JDIArgument, RequestInfo}
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.EventInfo
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, ScalaVirtualMachineManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success, Try}

class RequestHelperSpec extends ParallelMockFunSpec {
  // Define types for request helper
  // E: Event Type
  // EI: Event Info Type
  // RequestArgs: (Test String, Test Int, JDI Request Args)
  // CounterKey: (Test String, Test Int)
  private type E = Event
  private type EI = EventInfo
  private type RequestArgs = (String, Int, Seq[JDIRequestArgument])
  private type CounterKey = (String, Int)

  // Construct mocks used for testing
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockEventManager = mock[EventManager]
  private val testEventType = BreakpointEventType // NOTE: Mocking this results in StackOverflow
  private val mockNewRequestId = mockFunction[String]
  private val mockNewRequest = mockFunction[String, RequestArgs, Seq[JDIRequestArgument], Try[String]]
  private val mockHasRequest = mockFunction[RequestArgs, Boolean]
  private val mockRemoveRequestById = mockFunction[String, Unit]
  private val mockRetrieveRequestInfo = mockFunction[String, Option[RequestInfo]]
  private val mockNewEventInfo = mockFunction[ScalaVirtualMachine, E, Seq[JDIArgument], EI]

  // Create instance of helper per test
  private val requestHelper = {
    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = mockScalaVirtualMachine,
      eventManager = mockEventManager,
      etInstance = testEventType,
      _newRequestId = mockNewRequestId,
      _newRequest = mockNewRequest,
      _hasRequest = mockHasRequest,
      _removeRequestById = mockRemoveRequestById,
      _newEventInfo = mockNewEventInfo,
      _retrieveRequestInfo = mockRetrieveRequestInfo
    )
  }

  describe("RequestHelper") {
    describe("#newRequest") {
      it("should create a new request if one has not be made yet") {
        val expected = Success(UUID.randomUUID().toString)

        val requestArgs = ("test string", 999, Seq(mock[JDIRequestArgument]))
        val jdiRequestArgs = Seq(mock[JDIRequestArgument])

        // Check if request already exists (to invalidate cache if not)
        mockHasRequest.expects(requestArgs).returning(false).once()

        // Generate a new request id
        val requestId = expected.get
        mockNewRequestId.expects().returning(requestId).once()

        // Generate new request including unique id property
        mockNewRequest.expects(
          requestId,
          requestArgs,
          UniqueIdProperty(id = requestId) +: jdiRequestArgs
        ).returning(expected).once()

        val actual = requestHelper.newRequest(requestArgs, jdiRequestArgs)

        actual should be (expected)
      }

      it("should capture exceptions thrown when checking cache for request") {
        val expected = Failure(new Throwable)

        val requestArgs = ("test string", 999, Seq(mock[JDIRequestArgument]))
        val jdiRequestArgs = Seq(mock[JDIRequestArgument])

        // Check if request already exists (to invalidate cache if not)
        mockHasRequest.expects(requestArgs)
          .throwing(expected.failed.get).once()

        val actual = requestHelper.newRequest(requestArgs, jdiRequestArgs)

        actual should be (expected)
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)

        val requestArgs = ("test string", 999, Seq(mock[JDIRequestArgument]))
        val jdiRequestArgs = Seq(mock[JDIRequestArgument])

        // Check if request already exists (to invalidate cache if not)
        mockHasRequest.expects(requestArgs).returning(false).once()

        // Throw an error immediately
        mockNewRequestId.expects().throwing(expected.failed.get).once()

        val actual = requestHelper.newRequest(requestArgs, jdiRequestArgs)

        actual should be (expected)
      }

      it("should not create a new request if the previous one still exists") {
        val expected = Success(UUID.randomUUID().toString)

        val requestArgs = ("test string", 999, Seq(mock[JDIRequestArgument]))
        val jdiRequestArgs = Seq(mock[JDIRequestArgument])

        // Cache is invalid (no request exists)
        mockHasRequest.expects(requestArgs).returning(false).once()

        // Generate a new request id (once)
        val requestId = expected.get
        mockNewRequestId.expects().returning(requestId).once()

        // Generate new request including unique id property (once)
        mockNewRequest.expects(
          requestId,
          requestArgs,
          UniqueIdProperty(id = requestId) +: jdiRequestArgs
        ).returning(expected).once()

        // Create our request the first time
        requestHelper.newRequest(requestArgs, jdiRequestArgs)

        // Cache is valid (request exists)
        mockHasRequest.expects(requestArgs).returning(true).once()

        // Get our request the second time
        val actual = requestHelper.newRequest(requestArgs, jdiRequestArgs)

        actual should be (expected)
      }
    }

    describe("#newEventPipeline") {
      describe("when creating the pipeline") {
        it("should pass along the request and event arguments to the new event info") {
          val requestId = UUID.randomUUID().toString
          val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
          val mockJdiEventArgs = Seq(mock[JDIEventArgument])
          val counterKey = ("test string", 999)

          // Unique id is added
          val updatedJdiEventArgs = UniqueIdPropertyFilter(id = requestId) +:
            mockJdiEventArgs

          val mockEvent = mock[E]
          val mockEventInfo = mock[EI]
          val mockJdiEventDataResults = Seq(mock[JDIEventDataResult])

          // Retrieve any request info to extract extra arguments
          val mockRequestInfo = mock[RequestInfo]
          mockRetrieveRequestInfo.expects(requestId)
            .returning(Some(mockRequestInfo)).once()
          (mockRequestInfo.extraArguments _).expects()
            .returning(mockJdiRequestArgs).once()

          // Create base event pipeline (raw event coming from JDI)
          val eventPipeline = Pipeline.newPipeline(
            classOf[(E, Seq[JDIEventDataResult])],
            Map(EventManager.EventHandlerIdMetadataField -> requestId)
          )
          (mockEventManager.addEventDataStream _)
            .expects(testEventType, updatedJdiEventArgs)
            .returning(eventPipeline).once()

          // Transform into event info pipeline (added as child to event pipeline)
          requestHelper.newEventPipeline(
            requestId,
            mockJdiEventArgs,
            counterKey
          )

          // Validate that we construct a new Scala virtual machine manager
          // that obtains a Scala virtual machine associated with the event
          val mockVirtualMachine = mock[VirtualMachine]
          val mockScalaVirtualMachineManager = mock[ScalaVirtualMachineManager]
          (mockEvent.virtualMachine _).expects()
            .returning(mockVirtualMachine).once()
          (mockScalaVirtualMachine.manager _).expects()
            .returning(mockScalaVirtualMachineManager).once()
          (mockScalaVirtualMachineManager.apply(_: VirtualMachine))
            .expects(mockVirtualMachine)
            .returning(mockScalaVirtualMachine)
            .once()

          // Validate that each event gets transformed into the event info
          mockNewEventInfo.expects(
            mockScalaVirtualMachine,
            mockEvent,
            mockJdiRequestArgs ++ updatedJdiEventArgs
          ).returning(mockEventInfo).once()

          // Process a new event
          val (event, dataResults) = eventPipeline.process(
            (mockEvent, mockJdiEventDataResults)
          ).get.head

          event should be (mockEvent)
          dataResults should be (mockJdiEventDataResults)
        }
      }

      describe("when closing the generated pipelines for a specific request") {
        it("should remove the underlying request if all pipelines are closed") {
          val totalPipelines = 3
          val requestId = UUID.randomUUID().toString
          val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
          val mockJdiEventArgs = Seq(mock[JDIEventArgument])
          val counterKey = ("test string", 999)

          // Retrieve any request info to extract extra arguments
          val mockRequestInfo = mock[RequestInfo]
          mockRetrieveRequestInfo.expects(*)
            .returning(Some(mockRequestInfo))
            .repeated(totalPipelines).times()
          (mockRequestInfo.extraArguments _).expects()
            .returning(mockJdiRequestArgs)
            .repeated(totalPipelines).times()

          // Create N new pipelines
          val pipelines = (1 to totalPipelines).map(_ => {
            // Create base event pipeline (raw event coming from JDI)
            val eventPipeline = Pipeline.newPipeline(
              classOf[(E, Seq[JDIEventDataResult])],
              Map(EventManager.EventHandlerIdMetadataField -> requestId)
            )
            (mockEventManager.addEventDataStream _).expects(*, *)
              .returning(eventPipeline)
              .repeated(totalPipelines).once()

            requestHelper.newEventPipeline(
              requestId,
              mockJdiEventArgs,
              counterKey
            ).get
          })

          // Expect removing the request and removing the underlying event
          // handlers for the N pipelines
          mockRemoveRequestById.expects(requestId).once()
          (mockEventManager.removeEventHandler _).expects(requestId)
            .repeated(totalPipelines).times()

          // Close the N pipelines
          pipelines.foreach(_.close())
        }

        it("should remove the underlying request if close data says to do so") {
          val totalPipelines = 3
          val requestId = UUID.randomUUID().toString
          val mockJdiRequestArgs = Seq(mock[JDIRequestArgument])
          val mockJdiEventArgs = Seq(mock[JDIEventArgument])
          val counterKey = ("test string", 999)

          // Retrieve any request info to extract extra arguments
          val mockRequestInfo = mock[RequestInfo]
          mockRetrieveRequestInfo.expects(*)
            .returning(Some(mockRequestInfo))
            .repeated(totalPipelines).times()
          (mockRequestInfo.extraArguments _).expects()
            .returning(mockJdiRequestArgs)
            .repeated(totalPipelines).times()

          // Create N new pipelines
          val pipelines = (1 to totalPipelines).map(_ => {
            // Create base event pipeline (raw event coming from JDI)
            val eventPipeline = Pipeline.newPipeline(
              classOf[(E, Seq[JDIEventDataResult])],
              Map(EventManager.EventHandlerIdMetadataField -> requestId)
            )
            (mockEventManager.addEventDataStream _).expects(*, *)
              .returning(eventPipeline)
              .repeated(totalPipelines).once()

            requestHelper.newEventPipeline(
              requestId,
              mockJdiEventArgs,
              counterKey
            ).get
          })

          // Expect removing the request and removing the underlying event
          // handlers for the N pipelines
          mockRemoveRequestById.expects(requestId).once()
          (mockEventManager.removeEventHandler _).expects(requestId)
            .repeated(totalPipelines).times()

          // Close 1 pipeline and force the close of all others
          import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
          pipelines.head.close(data = CloseRemoveAll)
        }
      }
    }
  }
}
