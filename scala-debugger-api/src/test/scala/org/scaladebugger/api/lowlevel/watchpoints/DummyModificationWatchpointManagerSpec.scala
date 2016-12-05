package org.scaladebugger.api.lowlevel.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import test.JDIMockHelpers

class DummyModificationWatchpointManagerSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val modificationWatchpointManager = new DummyModificationWatchpointManager

  describe("DummyModificationWatchpointManager") {
    describe("#modificationWatchpointRequestListById") {
      it("should return an empty list") {
        modificationWatchpointManager.modificationWatchpointRequestListById should be (empty)
      }
    }

    describe("#modificationWatchpointRequestList") {
      it("should return an empty list") {
        modificationWatchpointManager.modificationWatchpointRequestList should be (empty)
      }
    }

    describe("#createModificationWatchpointRequestWithId") {
      it("should return a failure of dummy operation") {
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val result = modificationWatchpointManager.createModificationWatchpointRequestWithId(
          TestRequestId,
          testClassName,
          testFieldName
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasModificationWatchpointRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = modificationWatchpointManager.hasModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#hasModificationWatchpointRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = modificationWatchpointManager.hasModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }

    describe("#getModificationWatchpointRequestInfoWithId") {
      it("should return None") {
        val expected = None

        val actual = modificationWatchpointManager.getModificationWatchpointRequestInfoWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getModificationWatchpointRequestWithId") {
      it("should return None") {
        val expected = None

        val actual = modificationWatchpointManager.getModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getModificationWatchpointRequest") {
      it("should return None") {
        val expected = None
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = modificationWatchpointManager.getModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = modificationWatchpointManager.removeModificationWatchpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeModificationWatchpointRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"

        val actual = modificationWatchpointManager.removeModificationWatchpointRequest(
          testClassName,
          testFieldName
        )

        actual should be (expected)
      }
    }
  }
}
