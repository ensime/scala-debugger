package org.scaladebugger.api.lowlevel.threads
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestThreadStartManager

import scala.util.Success

class ThreadStartManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadStartManager = mock[ThreadStartManager]
  private val testThreadStartManager = new TestThreadStartManager(
    mockThreadStartManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ThreadStartManager") {
    describe("#createThreadStartRequest") {
      it("should invoke createThreadStartRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testThreadStartManager.createThreadStartRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createThreadStartRequestFromInfo") {
      it("should invoke createThreadStartRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadStartManager.createThreadStartRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = ThreadStartRequestInfo(
          TestRequestId,
          testIsPending,
          testExtraArguments
        )
        val actual = testThreadStartManager.createThreadStartRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
