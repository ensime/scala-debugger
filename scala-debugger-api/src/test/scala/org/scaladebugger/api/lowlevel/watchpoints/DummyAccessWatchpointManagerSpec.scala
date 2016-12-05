package org.scaladebugger.api.lowlevel.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import test.JDIMockHelpers

class DummyAccessWatchpointManagerSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val accessWatchpointManager = new DummyAccessWatchpointManager

  describe("DummyAccessWatchpointManager") {
    describe("#accessWatchpointRequestListById") {
      it("should return an empty list") {
        accessWatchpointManager.accessWatchpointRequestListById should be (empty)
      }
    }

    describe("#accessWatchpointRequestList") {
      it("should return an empty list") {
        accessWatchpointManager.accessWatchpointRequestList should be (empty)
      }
    }

    describe("#createAccessWatchpointRequestWithId") {
      it("should return a failure of dummy operation") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val result = accessWatchpointManager.createAccessWatchpointRequestWithId(
          TestRequestId,
          testClassName,
          testFieldName
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasAccessWatchpointRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = accessWatchpointManager.hasAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#hasAccessWatchpointRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = accessWatchpointManager.hasAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }

    describe("#getAccessWatchpointRequestInfoWithId") {
      it("should return None") {
        val expected = None

        val actual = accessWatchpointManager.getAccessWatchpointRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getAccessWatchpointRequestWithId") {
      it("should return None") {
        val expected = None

        val actual = accessWatchpointManager.getAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getAccessWatchpointRequest") {
      it("should return None") {
        val expected = None
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = accessWatchpointManager.getAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = accessWatchpointManager.removeAccessWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = accessWatchpointManager.removeAccessWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }
  }
}
