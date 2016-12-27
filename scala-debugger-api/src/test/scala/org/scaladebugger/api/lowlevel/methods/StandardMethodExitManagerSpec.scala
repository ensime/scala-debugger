package org.scaladebugger.api.lowlevel.methods
import com.sun.jdi.request.{EventRequest, EventRequestManager, MethodExitRequest}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Failure, Success}

class StandardMethodExitManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]
  private val mockClassManager = mock[ClassManager]

  private val methodExitManager = new StandardMethodExitManager(mockEventRequestManager, mockClassManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardMethodExitManager") {
    describe("#methodExitRequestList") {
      it("should contain all method exit requests in the form of (class, method) stored in the manager") {
        val methodExitRequests = Seq(
          MethodExitRequestInfo(TestRequestId, false, "class1", "method1"),
          MethodExitRequestInfo(TestRequestId + 1, false, "class2", "method2")
        )

        // NOTE: Must create a new method exit manager that does NOT override
        //       the request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val methodExitManager = new StandardMethodExitManager(mockEventRequestManager, mockClassManager)

        methodExitRequests.foreach { case MethodExitRequestInfo(requestId, _, className, methodName, _) =>
          (mockClassManager.hasMethodWithName _)
            .expects(className, methodName)
            .returning(true).once()

          (mockEventRequestManager.createMethodExitRequest _).expects()
            .returning(stub[MethodExitRequest]).once()
          methodExitManager.createMethodExitRequestWithId(requestId, className, methodName)
        }

        methodExitManager.methodExitRequestList should
          contain theSameElementsAs (methodExitRequests)
      }
    }

    describe("#methodExitRequestListById") {
      it("should contain all method exit request ids") {
        val methodExitRequests = Seq(
          ("id1", "class1", "method1"),
          ("id2", "class2", "method2")
        )

        methodExitRequests.foreach { case (requestId, className, methodName) =>
          (mockClassManager.hasMethodWithName _)
            .expects(className, methodName)
            .returning(true).once()

          (mockEventRequestManager.createMethodExitRequest _).expects()
            .returning(stub[MethodExitRequest]).once()
          methodExitManager.createMethodExitRequestWithId(
            requestId,
            className,
            methodName
          )
        }

        methodExitManager.methodExitRequestListById should
          contain theSameElementsAs (methodExitRequests.map(_._1))
      }
    }

    describe("#createMethodExitRequestWithId") {
      it("should create the method exit request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        val mockMethodExitRequest = mock[MethodExitRequest]
        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(mockMethodExitRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodExitRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodExitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodExitRequest.setEnabled _).expects(true).once()

        val actual = methodExitManager.createMethodExitRequestWithId(
          expected.get,
          testClassName,
          testMethodName
        )
        actual should be(expected)
      }

      it("should create the method exit request with a class inclusion filter for the class name") {
        val expected = Success(TestRequestId)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        val mockMethodExitRequest = mock[MethodExitRequest]
        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(mockMethodExitRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodExitRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodExitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodExitRequest.setEnabled _).expects(true).once()

        val actual = methodExitManager.createMethodExitRequestWithId(
          expected.get,
          testClassName,
          testMethodName
        )
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )
        actual should be (expected)
      }

      it("should fail if the class of the method or method itself does not exist") {
        val testClassName = "some class name"
        val testMethodName = "some method name"
        val expected = Failure(NoClassMethodFound(testClassName, testMethodName))

        // Does not exist
        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(false).once()

        val actual = methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )
        actual should be (expected)
      }
    }

    describe("#hasMethodExitRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodExitManager.hasMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = methodExitManager.hasMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasMethodExitRequest") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        val actual = methodExitManager.hasMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual = methodExitManager.hasMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#getMethodExitRequestInfoWithId") {
      it("should return Some(MethodExitRequestInfo(id, not pending, class name, method name)) if the id exists") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val expected = Some(MethodExitRequestInfo(
          requestId = TestRequestId,
          isPending = false,
          className = "some.class.name",
          methodName = "someMethodName"
        ))

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.createMethodExitRequestWithId(
          expected.get.requestId,
          expected.get.className,
          expected.get.methodName
        )

        val actual = methodExitManager.getMethodExitRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return None if there is no breakpoint with the id") {
        val expected = None

        val actual = methodExitManager.getMethodExitRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMethodExitRequestWithId") {
      it("should return Some(MethodExitRequest) if found") {
        val expected = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(expected).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodExitManager.getMethodExitRequestWithId(TestRequestId)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = methodExitManager.getMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMethodExitRequest") {
      it("should return Some(collection of MethodExitRequest) if found") {
        val expected = Seq(stub[MethodExitRequest])

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(expected.head).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        val actual =
          methodExitManager.getMethodExitRequest(testClassName, testMethodName)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.getMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequestWithId") {
      it("should return true if the method exit request was removed") {
        val expected = true
        val stubRequest = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stubRequest).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodExitManager.removeMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the method exit request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.removeMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequest") {
      it("should return true if the method exit request was removed") {
        val expected = true
        val stubRequest = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockClassManager.hasMethodWithName _)
          .expects(testClassName, testMethodName)
          .returning(true).once()

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stubRequest).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodExitManager.removeMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if the method exit request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.removeMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }
  }
}
