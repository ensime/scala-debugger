package org.scaladebugger.api.lowlevel.monitors
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestMonitorContendedEnterManager

import scala.util.Success

class MonitorContendedEnterManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
  private val testMonitorContendedEnterManager = new TestMonitorContendedEnterManager(
    mockMonitorContendedEnterManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MonitorContendedEnterManager") {
    describe("#createMonitorContendedEnterRequest") {
      it("should invoke createMonitorContendedEnterRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testMonitorContendedEnterManager.createMonitorContendedEnterRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createMonitorContendedEnterRequestFromInfo") {
      it("should invoke createMonitorContendedEnterRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = MonitorContendedEnterRequestInfo(
          TestRequestId,
          testIsPending,
          testExtraArguments
        )
        val actual = testMonitorContendedEnterManager.createMonitorContendedEnterRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
