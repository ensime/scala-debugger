package org.scaladebugger.api.lowlevel.threads

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException

class DummyThreadDeathManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val threadDeathManager = new DummyThreadDeathManager

  describe("DummyThreadDeathManager") {
    describe("#threadDeathRequestList") {
      it("should return an empty list") {
        threadDeathManager.threadDeathRequestList should be (empty)
      }
    }

    describe("#createThreadDeathRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = threadDeathManager.createThreadDeathRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasThreadDeathRequest") {
      it("should return false") {
        val expected = false

        val actual = threadDeathManager.hasThreadDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getThreadDeathRequest") {
      it("should return None") {
        val expected = None

        val actual = threadDeathManager.getThreadDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getThreadDeathRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = threadDeathManager.getThreadDeathRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeThreadDeathRequest") {
      it("should return false") {
        val expected = false

        val actual = threadDeathManager.removeThreadDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
