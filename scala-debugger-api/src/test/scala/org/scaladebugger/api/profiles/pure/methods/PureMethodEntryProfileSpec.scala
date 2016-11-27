package org.scaladebugger.api.profiles.pure.methods
import acyclic.file
import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.MethodEntryEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.filters.{MethodNameFilter, UniqueIdPropertyFilter}
import org.scaladebugger.api.lowlevel.methods.{MethodEntryManager, MethodEntryRequestInfo, PendingMethodEntrySupportLike}
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

class PureMethodEntryProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodEntryManager = mock[MethodEntryManager]
  private val mockEventManager = mock[EventManager]
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

  private val pureMethodEntryProfile = new Object with PureMethodEntryProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newMethodEntryRequestId(): String =
      if (requestId != null) requestId else super.newMethodEntryRequestId()

    override protected val methodEntryManager = mockMethodEntryManager
    override protected val eventManager: EventManager = mockEventManager
    override protected val infoProducer: InfoProducerProfile = mockInfoProducer
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
  }

  describe("PureMethodEntryProfile") {
    describe("#methodEntryRequests") {
      it("should include all active requests") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodEntryManager = mock[PendingMethodEntrySupportLike]
        val pureMethodEntryProfile = new Object with PureMethodEntryProfile {
          override protected val methodEntryManager = mockMethodEntryManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()

        (mockMethodEntryManager.pendingMethodEntryRequests _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }

      it("should include pending requests if supported") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            true,
            "some.class.name",
            "someMethodName"
          )
        )

        val mockMethodEntryManager = mock[PendingMethodEntrySupportLike]
        val pureMethodEntryProfile = new Object with PureMethodEntryProfile {
          override protected val methodEntryManager = mockMethodEntryManager
          override protected val eventManager: EventManager = mockEventManager
          override protected val infoProducer: InfoProducerProfile = mockInfoProducer
          override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
        }

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        (mockMethodEntryManager.pendingMethodEntryRequests _).expects()
          .returning(expected).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }

      it("should only include active requests if pending unsupported") {
        val expected = Seq(
          MethodEntryRequestInfo(
            TestRequestId,
            false,
            "some.class.name",
            "someMethodName"
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()

        val actual = pureMethodEntryProfile.methodEntryRequests

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching filename exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return empty if no request with matching line number exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequests(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequestWithArgs") {
      it("should return None if no requests exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return None if no request with matching filename exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching line number exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return None if no request with matching extra arguments exists") {
        val expected = None
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return remove and return matching pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should remove and return matching non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Some(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Seq(expected.get)).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeMethodEntryRequestWithArgs(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeAllMethodEntryRequests") {
      it("should return empty if no requests exists") {
        val expected = Nil
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }

      it("should remove and return all pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }

      it("should remove and return all non-pending requests") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val expected = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(expected).once()
        expected.foreach(b =>
          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(b.requestId)
            .returning(true)
            .once()
        )

        val actual = pureMethodEntryProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }
    }

    describe("#isMethodEntryRequestPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching class name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching method name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestPending(
          className,
          methodName
        )

        actual should be (expected)
      }
    }

    describe("#isMethodEntryRequestWithArgsPending") {
      it("should return false if no requests exist") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(Nil).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching class name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className + "other",
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching method name exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName + 1,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no request with matching extra arguments exists") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName
        )

        actual should be (expected)
      }

      it("should return false if no matching request is pending") {
        val expected = false
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = false,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should return true if at least one matching request is pending") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIRequestArgument])

        val requests = Seq(
          MethodEntryRequestInfo(
            requestId = TestRequestId,
            isPending = true,
            className = className,
            methodName = methodName,
            extraArguments = extraArguments
          )
        )

        (mockMethodEntryManager.methodEntryRequestList _).expects()
          .returning(requests).once()

        val actual = pureMethodEntryProfile.isMethodEntryRequestWithArgsPending(
          className,
          methodName,
          extraArguments: _*
        )

        actual should be (expected)
      }
    }
    
    describe("#tryGetOrCreateMethodEntryRequestWithData") {
      it("should create a new request if one has not be made yet") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)
        val eventArguments = Seq(
          uniqueIdPropertyFilter,
          MethodNameFilter(methodName)
        )

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId,
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId,
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId,
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Return false this time to indicate that the methodEntry request
          // was removed some time between the two calls
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId + "other",
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId,
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Return true to indicate that we do still have the request
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should create a new request for different input") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId,
            className,
            methodName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodEntryProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName + 1)
          )

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodEntryManager.hasMethodEntryRequest _)
            .expects(className, methodName + 1)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
            TestRequestId + "other",
            className,
            methodName + 1,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodEntryEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName + 1,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            val eventArguments = Seq(
              uniqueIdPropertyFilter,
              MethodNameFilter(methodName)
            )

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockMethodEntryManager.hasMethodEntryRequest _)
              .expects(className, methodName)
              .returning(false).once()
            (mockMethodEntryManager.hasMethodEntryRequest _)
              .expects(className, methodName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
              TestRequestId,
              className,
              methodName,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(MethodEntryEventType, eventArguments)
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )
        val p2 = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }

      it("should remove the underlying request if close data says to do so") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMethodEntryProfile.setRequestId(TestRequestId)

        inSequence {
          val eventHandlerIds = Seq("a", "b")
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            val eventArguments = Seq(
              uniqueIdPropertyFilter,
              MethodNameFilter(methodName)
            )

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockMethodEntryManager.hasMethodEntryRequest _)
              .expects(className, methodName)
              .returning(false).once()
            (mockMethodEntryManager.hasMethodEntryRequest _)
              .expects(className, methodName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockMethodEntryManager.createMethodEntryRequestWithId _).expects(
              TestRequestId,
              className,
              methodName,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            // NOTE: Pipeline adds an event handler id to its metadata
            def newEventPipeline(id: String) = Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            ).withMetadata(Map(EventManager.EventHandlerIdMetadataField -> id))

            eventHandlerIds.foreach(id => {
              (mockEventManager.addEventDataStream _)
                .expects(MethodEntryEventType, eventArguments)
                .returning(newEventPipeline(id)).once()
            })
          }

          (mockMethodEntryManager.removeMethodEntryRequestWithId _)
            .expects(TestRequestId).once()
          eventHandlerIds.foreach(id => {
            (mockEventManager.removeEventHandler _).expects(id).once()
          })
        }

        val p1 = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )
        val p2 = pureMethodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
          className,
          methodName,
          arguments: _*
        )

        p1.foreach(_.close(now = true, data = Constants.CloseRemoveAll))
      }
    }
  }
}

