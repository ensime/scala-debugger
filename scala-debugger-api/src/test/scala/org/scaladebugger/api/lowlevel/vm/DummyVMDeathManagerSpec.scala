package org.scaladebugger.api.lowlevel.vm

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException

class DummyVMDeathManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val vmDeathManager = new DummyVMDeathManager

  describe("DummyVMDeathManager") {
    describe("#vmDeathRequestList") {
      it("should return an empty list") {
        vmDeathManager.vmDeathRequestList should be (empty)
      }
    }

    describe("#createVMDeathRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = vmDeathManager.createVMDeathRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasVMDeathRequest") {
      it("should return false") {
        val expected = false

        val actual = vmDeathManager.hasVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getVMDeathRequest") {
      it("should return None") {
        val expected = None

        val actual = vmDeathManager.getVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getVMDeathRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = vmDeathManager.getVMDeathRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeVMDeathRequest") {
      it("should return false") {
        val expected = false

        val actual = vmDeathManager.removeVMDeathRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
