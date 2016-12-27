package org.scaladebugger.api.lowlevel.methods

import com.sun.jdi.request.{EventRequest, EventRequestManager, MethodEntryRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.{Failure, Success}

class DummyMethodEntryManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val methodEntryManager = new DummyMethodEntryManager

  describe("DummyMethodEntryManager") {
    describe("#methodEntryRequestList") {
      it("should return an empty list") {
        methodEntryManager.methodEntryRequestList should be (empty)
      }
    }

    describe("#methodEntryRequestListById") {
      it("should return an empty list") {
        methodEntryManager.methodEntryRequestListById should be (empty)
      }
    }

    describe("#createMethodEntryRequestWithId") {
      it("should return a failure of dummy operation") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val result = methodEntryManager.createMethodEntryRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasMethodEntryRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = methodEntryManager.hasMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#hasMethodEntryRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodEntryManager.hasMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }

    describe("#getMethodEntryRequestInfoWithId") {
      it("should return None") {
        val expected = None

        val actual = methodEntryManager.getMethodEntryRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMethodEntryRequestWithId") {
      it("should return None") {
        val expected = None

        val actual = methodEntryManager.getMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMethodEntryRequest") {
      it("should return None") {
        val expected = None
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodEntryManager.getMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = methodEntryManager.removeMethodEntryRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodEntryManager.removeMethodEntryRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }
  }
}
