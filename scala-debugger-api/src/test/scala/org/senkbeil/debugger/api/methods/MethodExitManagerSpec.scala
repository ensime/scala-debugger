package org.senkbeil.debugger.api.methods

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.{EventRequest, EventRequestManager, MethodExitRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class MethodExitManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager).once()

  private val methodExitManager = new MethodExitManager(mockVirtualMachine)

  describe("MethodExitManager") {
    describe("#methodExitList") {
      it("should contain all method exit requests in the form of (class, method) stored in the manager") {
        val methodExitRequests = Seq(
          ("class1", "method1"),
          ("class2", "method2")
        )

        methodExitRequests.foreach { case (className, methodName) =>
          (mockEventRequestManager.createMethodExitRequest _).expects()
            .returning(stub[MethodExitRequest]).once()
          methodExitManager.setMethodExit(className, methodName)
        }

        methodExitManager.methodExitList should
          contain theSameElementsAs (methodExitRequests)
      }
    }

    describe("#setMethodExit") {
      it("should create the method exit request with a class inclusion filter for the class name") {
        val testClassName = "some class name"
        val testMethodName = "some method name"

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

        methodExitManager.setMethodExit(testClassName, testMethodName) should be (true)
      }
    }

    describe("#hasMethodExit") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.setMethodExit(testClassName, testMethodName)

        val actual = methodExitManager.hasMethodExit(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual = methodExitManager.hasMethodExit(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#getMethodExit") {
      it("should return Some(MethodExitRequest) if found") {
        val expected = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(expected).once()

        methodExitManager.setMethodExit(testClassName, testMethodName)

        val actual =
          methodExitManager.getMethodExit(testClassName, testMethodName)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.getMethodExit(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#removeMethodExit") {
      it("should return true if the method exit request was removed") {
        val expected = true
        val stubRequest = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stubRequest).once()

        methodExitManager.setMethodExit(testClassName, testMethodName)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodExitManager.removeMethodExit(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if the method exit request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.removeMethodExit(testClassName, testMethodName)
        actual should be (expected)
      }
    }
  }
}
