package org.senkbeil.debugger.api.methods

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event
import com.sun.jdi.request.{EventRequest, MethodEntryRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

class MethodEntryManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager).once()

  private val methodEntryManager = new MethodEntryManager(mockVirtualMachine)

  describe("MethodEntryManager") {
    describe("#methodEntryList") {
      it("should contain all method entry requests in the form of (class, method) stored in the manager") {
        val methodEntryRequests = Seq(
          ("class1", "method1"),
          ("class2", "method2")
        )

        methodEntryRequests.foreach { case (className, methodName) =>
          (mockEventRequestManager.createMethodEntryRequest _).expects()
            .returning(stub[MethodEntryRequest]).once()
          methodEntryManager.setMethodEntry(className, methodName)
        }

        methodEntryManager.methodEntryList should
          contain theSameElementsAs (methodEntryRequests)
      }
    }

    describe("#setMethodEntry") {
      it("should create the method entry request with a class inclusion filter for the class name") {
        val testClassName = "some class name"
        val testMethodName = "some method name"

        val mockMethodEntryRequest = mock[MethodEntryRequest]
        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(mockMethodEntryRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodEntryRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodEntryRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodEntryRequest.setEnabled _).expects(true).once()

        methodEntryManager.setMethodEntry(testClassName, testMethodName) should be (true)
      }
    }

    describe("#hasMethodEntry") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stub[MethodEntryRequest]).once()

        methodEntryManager.setMethodEntry(testClassName, testMethodName)

        val actual = methodEntryManager.hasMethodEntry(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual = methodEntryManager.hasMethodEntry(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#getMethodEntry") {
      it("should return Some(MethodEntryRequest) if found") {
        val expected = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(expected).once()

        methodEntryManager.setMethodEntry(testClassName, testMethodName)

        val actual =
          methodEntryManager.getMethodEntry(testClassName, testMethodName)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodEntryManager.getMethodEntry(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#removeMethodEntry") {
      it("should return true if the method entry request was removed") {
        val expected = true
        val stubRequest = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stubRequest).once()

        methodEntryManager.setMethodEntry(testClassName, testMethodName)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodEntryManager.removeMethodEntry(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if the method entry request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodEntryManager.removeMethodEntry(testClassName, testMethodName)
        actual should be (expected)
      }
    }
  }
}
