package org.scaladebugger.api.profiles.pure.requests.vm
import acyclic.file
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.VMDeathEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.vm.{PendingVMDeathSupportLike, VMDeathManager, VMDeathRequestInfo}
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.Constants
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureVMDeathProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockVMDeathManager = mock[VMDeathManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureVMDeathProfile = new Object with PureVMDeathProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newVMDeathRequestId(): String =
      if (requestId != null) requestId else super.newVMDeathRequestId()

    override protected val vmDeathManager = mockVMDeathManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureVMDeathProfile") {
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

    describe("#tryGetOrCreateVMDeathRequestWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureVMDeathProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureVMDeathProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureVMDeathProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureVMDeathProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureVMDeathProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the vm death request
          // was removed some time between the two calls
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(TestRequestId + "other", uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureVMDeathProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockVMDeathManager.createVMDeathRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureVMDeathProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockVMDeathManager.vmDeathRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockVMDeathManager.getVMDeathRequestInfo _)
            .expects(internalId)
            .returning(Some(VMDeathRequestInfo(TestRequestId, false, arguments))).once()

          (mockEventManager.addEventDataStream _)
            .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureVMDeathProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockVMDeathManager.vmDeathRequestList _)
              .expects()
              .returning(Nil).once()
            (mockVMDeathManager.vmDeathRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockVMDeathManager.getVMDeathRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(VMDeathRequestInfo(TestRequestId, false, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockVMDeathManager.createVMDeathRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)
        val p2 = pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)

        p1.foreach(_.close())
        p2.foreach(_.close())
      }

      it("should remove the underlying request if close data says to do so") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureVMDeathProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockVMDeathManager.vmDeathRequestList _)
              .expects()
              .returning(Nil).once()
            (mockVMDeathManager.vmDeathRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockVMDeathManager.getVMDeathRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(VMDeathRequestInfo(TestRequestId, false, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockVMDeathManager.createVMDeathRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(VMDeathEventType, Seq(uniqueIdPropertyFilter))
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockVMDeathManager.removeVMDeathRequest _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)
        val p2 = pureVMDeathProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)

        p1.foreach(_.close(now = true, data = Constants.CloseRemoveAll))
      }
    }
  }
}

