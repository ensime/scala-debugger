package org.scaladebugger.api.lowlevel.methods
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException

class DummyMethodExitManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val methodExitManager = new DummyMethodExitManager

  describe("DummyMethodExitManager") {
    describe("#methodExitRequestList") {
      it("should return an empty list") {
        methodExitManager.methodExitRequestList should be (empty)
      }
    }

    describe("#methodExitRequestListById") {
      it("should return an empty list") {
        methodExitManager.methodExitRequestListById should be (empty)
      }
    }

    describe("#createMethodExitRequestWithId") {
      it("should return a failure of dummy operation") {
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val result = methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasMethodExitRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = methodExitManager.hasMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#hasMethodExitRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodExitManager.hasMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }

    describe("#getMethodExitRequestInfoWithId") {
      it("should return None") {
        val expected = None

        val actual = methodExitManager.getMethodExitRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMethodExitRequestWithId") {
      it("should return None") {
        val expected = None

        val actual = methodExitManager.getMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMethodExitRequest") {
      it("should return None") {
        val expected = None
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodExitManager.getMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = methodExitManager.removeMethodExitRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"

        val actual = methodExitManager.removeMethodExitRequest(
          testClassName,
          testMethodName
        )

        actual should be (expected)
      }
    }
  }
}
