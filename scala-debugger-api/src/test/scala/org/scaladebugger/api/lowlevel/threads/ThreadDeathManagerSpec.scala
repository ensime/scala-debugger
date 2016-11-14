package org.scaladebugger.api.lowlevel.threads
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestThreadDeathManager

import scala.util.Success

class ThreadDeathManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadDeathManager = mock[ThreadDeathManager]
  private val testThreadDeathManager = new TestThreadDeathManager(
    mockThreadDeathManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ThreadDeathManager") {
    describe("#createThreadDeathRequest") {
      it("should invoke createThreadDeathRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadDeathManager.createThreadDeathRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testThreadDeathManager.createThreadDeathRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createThreadDeathRequestFromInfo") {
      it("should invoke createThreadDeathRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockThreadDeathManager.createThreadDeathRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = ThreadDeathRequestInfo(
          TestRequestId,
          testIsPending,
          testExtraArguments
        )
        val actual = testThreadDeathManager.createThreadDeathRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
