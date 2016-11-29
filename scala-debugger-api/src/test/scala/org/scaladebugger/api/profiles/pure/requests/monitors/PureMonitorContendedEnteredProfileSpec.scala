package org.scaladebugger.api.profiles.pure.requests.monitors
import acyclic.file
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.MonitorContendedEnteredEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnteredManager, MonitorContendedEnteredRequestInfo, PendingMonitorContendedEnteredSupportLike}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureMonitorContendedEnteredProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnteredManager =
    mock[MonitorContendedEnteredManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureMonitorContendedEnteredProfile = new Object with PureMonitorContendedEnteredProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newMonitorContendedEnteredRequestId(): String =
      if (requestId != null) requestId else super.newMonitorContendedEnteredRequestId()

    override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureMonitorContendedEnteredProfile") {
    describe("#monitorContendedEnteredRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, false)
        )

        val mockMonitorContendedEnteredManager = mock[PendingMonitorContendedEnteredSupportLike]
        val pureMonitorContendedEnteredProfile = new Object with PureMonitorContendedEnteredProfile {
          override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        (mockMonitorContendedEnteredManager.pendingMonitorContendedEnteredRequests _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, true)
        )

        val mockMonitorContendedEnteredManager = mock[PendingMonitorContendedEnteredSupportLike]
        val pureMonitorContendedEnteredProfile = new Object with PureMonitorContendedEnteredProfile {
          override protected val monitorContendedEnteredManager = mockMonitorContendedEnteredManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(Nil).once()

        (mockMonitorContendedEnteredManager.pendingMonitorContendedEnteredRequests _).expects()
          .returning(expected).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MonitorContendedEnteredRequestInfo(TestRequestId, false)
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(expected.map(_.requestId)).once()
        (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
          .expects(TestRequestId).returning(expected.headOption).once()

        val actual = pureMonitorContendedEnteredProfile.monitorContendedEnteredRequests

        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnteredRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs()

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs()

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,

            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Seq(expected.get).map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeMonitorContendedEnteredRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllMonitorContendedEnteredRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
          .expects()
          .returning(expected.map(_.requestId)).once()
        expected.foreach(r => {
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(r.requestId)
            .returning(true)
            .once()
        })

        val actual = pureMonitorContendedEnteredProfile.removeAllMonitorContendedEnteredRequests()

        actual should be (expected)
      }
    }

    describe("#isMonitorContendedEnteredRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending()

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MonitorContendedEnteredRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            extraArguments = extraArguments
          )
        )

        (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _).expects()
          .returning(requests.map(_.requestId)).once()
        requests.foreach(r =>
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(r.requestId)
            .returning(Some(r))
            .once()
        )

        val actual = pureMonitorContendedEnteredProfile.isMonitorContendedEnteredRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#tryGetOrCreateMonitorContendedEnteredRequestWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
            classOf[(Event, Seq[JDIEventDataResult])]
          )).once()
        }

        pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )

        actual should be(expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
            classOf[(Event, Seq[JDIEventDataResult])]
          )).once()
        }

        pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnteredProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the vm death request
          // was removed some time between the two calls
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(TestRequestId + "other", uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
            classOf[(Event, Seq[JDIEventDataResult])]
          )).once()
        }

        pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
            classOf[(Event, Seq[JDIEventDataResult])]
          )).once()
        }

        pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnteredProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
            .expects(internalId)
            .returning(Some(MonitorContendedEnteredRequestInfo(TestRequestId, false, arguments))).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
            classOf[(Event, Seq[JDIEventDataResult])]
          )).once()
        }

        pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
              .expects()
              .returning(Nil).once()
            (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(MonitorContendedEnteredRequestInfo(TestRequestId, false, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)
        val p2 = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)

        p1.foreach(_.close())
        p2.foreach(_.close())
      }

      it("should remove the underlying request if close data says to do so") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnteredProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
              .expects()
              .returning(Nil).once()
            (mockMonitorContendedEnteredManager.monitorContendedEnteredRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockMonitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(MonitorContendedEnteredRequestInfo(TestRequestId, false, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockMonitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(MonitorContendedEnteredEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockMonitorContendedEnteredManager.removeMonitorContendedEnteredRequest _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)
        val p2 = pureMonitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)

        p1.foreach(_.close(now = true, data = Constants.CloseRemoveAll))
      }
    }
  }
}
