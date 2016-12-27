package org.scaladebugger.api.lowlevel.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class DummyMonitorWaitedManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val monitorWaitedManager = new DummyMonitorWaitedManager

  describe("DummyMonitorWaitedManager") {
    describe("#monitorWaitedRequestList") {
      it("should return an empty list") {
        monitorWaitedManager.monitorWaitedRequestList should be (empty)
      }
    }

    describe("#createMonitorWaitedRequestWithId") {
      it("should return a failure of dummy operation") {
        val result = monitorWaitedManager.createMonitorWaitedRequestWithId(
          TestRequestId
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasMonitorWaitedRequest") {
      it("should return false") {
        val expected = false

        val actual = monitorWaitedManager.hasMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedRequest") {
      it("should return None") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedRequestInfo") {
      it("should return None") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequestInfo(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitedRequest") {
      it("should return false") {
        val expected = false

        val actual = monitorWaitedManager.removeMonitorWaitedRequest(
          TestRequestId
        )

        actual should be (expected)
      }
    }
  }
}
