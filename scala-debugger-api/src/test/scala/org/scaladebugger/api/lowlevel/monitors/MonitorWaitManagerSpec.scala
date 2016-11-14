package org.scaladebugger.api.lowlevel.monitors
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestMonitorWaitManager

import scala.util.Success

class MonitorWaitManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorWaitManager = mock[MonitorWaitManager]
  private val testMonitorWaitManager = new TestMonitorWaitManager(
    mockMonitorWaitManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MonitorWaitManager") {
    describe("#createMonitorWaitRequest") {
      it("should invoke createMonitorWaitRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testMonitorWaitManager.createMonitorWaitRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createMonitorWaitRequestFromInfo") {
      it("should invoke createMonitorWaitRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorWaitManager.createMonitorWaitRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = MonitorWaitRequestInfo(
          TestRequestId,
          testIsPending,
          testExtraArguments
        )
        val actual = testMonitorWaitManager.createMonitorWaitRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
