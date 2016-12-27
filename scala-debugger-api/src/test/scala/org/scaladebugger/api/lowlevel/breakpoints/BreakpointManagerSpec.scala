package org.scaladebugger.api.lowlevel.breakpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.TestBreakpointManager

import scala.util.Success

class BreakpointManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockBreakpointManager = mock[BreakpointManager]
  private val testBreakpointManager = new TestBreakpointManager(
    mockBreakpointManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("BreakpointManager") {
    describe("#createBreakpointRequest") {
      it("should invoke createBreakpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, testExtraArguments)
          .returning(expected).once()

        val actual = testBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequestFromInfo") {
      it("should invoke createBreakpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, testExtraArguments)
          .returning(expected).once()

        val info = BreakpointRequestInfo(
          TestRequestId,
          testIsPending,
          testFileName,
          testLineNumber,
          testExtraArguments
        )
        val actual = testBreakpointManager.createBreakpointRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
