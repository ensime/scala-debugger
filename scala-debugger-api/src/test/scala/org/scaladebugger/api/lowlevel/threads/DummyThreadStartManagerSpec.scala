package org.scaladebugger.api.lowlevel.threads

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class DummyThreadStartManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val threadStartManager = new DummyThreadStartManager

  describe("DummyThreadStartManager") {
    describe("#threadStartRequestList") {
      it("should return an empty list") {
        threadStartManager.threadStartRequestList should be (empty)
      }
    }

    describe("#createThreadStartRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = threadStartManager.createThreadStartRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasThreadStartRequest") {
      it("should return false") {
        val expected = false

        val actual = threadStartManager.hasThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getThreadStartRequest") {
      it("should return None") {
        val expected = None

        val actual = threadStartManager.getThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getThreadStartRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = threadStartManager.getThreadStartRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeThreadStartRequest") {
      it("should return false") {
        val expected = false

        val actual = threadStartManager.removeThreadStartRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
