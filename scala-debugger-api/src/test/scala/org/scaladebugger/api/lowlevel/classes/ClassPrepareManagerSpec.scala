package org.scaladebugger.api.lowlevel.classes

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestClassPrepareManager

import scala.util.Success

class ClassPrepareManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassPrepareManager = mock[ClassPrepareManager]
  private val testClassPrepareManager = new TestClassPrepareManager(
    mockClassPrepareManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ClassPrepareManager") {
    describe("#createClassPrepareRequest") {
      it("should invoke createClassPrepareRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testClassPrepareManager.createClassPrepareRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createClassPrepareRequestFromInfo") {
      it("should invoke createClassPrepareRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockClassPrepareManager.createClassPrepareRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = ClassPrepareRequestInfo(
          TestRequestId,
          testIsPending,
          testExtraArguments
        )
        val actual = testClassPrepareManager.createClassPrepareRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
