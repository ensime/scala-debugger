package org.scaladebugger.api.lowlevel.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class DummyMonitorContendedEnteredManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val monitorContendedEnteredManager = new DummyMonitorContendedEnteredManager

  describe("DummyMonitorContendedEnteredManager") {
    describe("#monitorContendedEnteredRequestList") {
      it("should return an empty list") {
        monitorContendedEnteredManager.monitorContendedEnteredRequestList should be (empty)
      }
    }

    describe("#createMonitorContendedEnteredRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = monitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasMonitorContendedEnteredRequest") {
      it("should return false") {
        val expected = false

        val actual = monitorContendedEnteredManager.hasMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnteredRequest") {
      it("should return None") {
        val expected = None

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnteredRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnteredRequest") {
      it("should return false") {
        val expected = false

        val actual = monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
