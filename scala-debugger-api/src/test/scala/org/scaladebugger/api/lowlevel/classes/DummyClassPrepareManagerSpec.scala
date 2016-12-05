package org.scaladebugger.api.lowlevel.classes

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException

class DummyClassPrepareManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val classPrepareManager = new DummyClassPrepareManager

  describe("DummyClassPrepareManager") {
    describe("#classPrepareRequestList") {
      it("should return an empty list") {
        classPrepareManager.classPrepareRequestList should be (empty)
      }
    }

    describe("#createClassPrepareRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = classPrepareManager.createClassPrepareRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasClassPrepareRequest") {
      it("should return false") {
        val expected = false

        val actual = classPrepareManager.hasClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getClassPrepareRequest") {
      it("should return None") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getClassPrepareRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeClassPrepareRequest") {
      it("should return false") {
        val expected = false

        val actual = classPrepareManager.removeClassPrepareRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
