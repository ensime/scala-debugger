package org.senkbeil.debugger.api.profiles.pure.methods

import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.{UniqueIdPropertyFilter, MethodNameFilter}
import org.senkbeil.debugger.api.lowlevel.methods.MethodExitManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers
import org.senkbeil.debugger.api.lowlevel.events.EventType.MethodExitEventType

import scala.util.{Failure, Success}

class PureMethodExitProfileSpec extends FunSpec with Matchers
with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgMethodExitManager extends MethodExitManager(
    stub[EventRequestManager]
  )
  private val mockMethodExitManager = mock[ZeroArgMethodExitManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureMethodExitProfile = new Object with PureMethodExitProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newMethodExitRequestId(): String =
      if (requestId != null) requestId else super.newMethodExitRequestId()

    override protected val methodExitManager = mockMethodExitManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureMethodExitProfile") {
    describe("#onMethodExitWithData") {
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
        pureMethodExitProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
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
        pureMethodExitProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureMethodExitProfile.onMethodExitWithData(
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
        pureMethodExitProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodExitProfile.setRequestId(TestRequestId)

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
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodExitProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Return false this time to indicate that the methodExit request
          // was removed some time between the two calls
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
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
          pureMethodExitProfile.setRequestId(TestRequestId)

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
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodExitProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)
          val eventArguments = Seq(
            uniqueIdPropertyFilter,
            MethodNameFilter(methodName)
          )

          // Return true to indicate that we do still have the request
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
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
        pureMethodExitProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodExitProfile.setRequestId(TestRequestId)

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
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMethodExitProfile.setRequestId(TestRequestId + "other")

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
          (mockMethodExitManager.hasMethodExitRequest _)
            .expects(className, methodName + 1)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMethodExitManager.createMethodExitRequest _)
            .expects(className, methodName + 1, uniqueIdProperty +: arguments)
            .returning(Success(true)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MethodExitEventType, eventArguments)
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName + 1,
          arguments: _*
        )
      }
    }
  }
}
