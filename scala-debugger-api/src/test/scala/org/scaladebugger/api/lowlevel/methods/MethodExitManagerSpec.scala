package org.scaladebugger.api.lowlevel.methods

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.TestMethodExitManager

import scala.util.Success

class MethodExitManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodExitManager = mock[MethodExitManager]
  private val testMethodExitManager = new TestMethodExitManager(
    mockMethodExitManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MethodExitManager") {
    describe("#createMethodExitRequest") {
      it("should invoke createMethodExitRequestWithId") {
        val expected = Success(TestRequestId)
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMethodExitManager.createMethodExitRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, testExtraArguments)
          .returning(expected).once()

        val actual = testMethodExitManager.createMethodExitRequest(
          testClassName,
          testMethodName,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createMethodExitRequestFromInfo") {
      it("should invoke createMethodExitRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMethodExitManager.createMethodExitRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, testExtraArguments)
          .returning(expected).once()

        val info = MethodExitRequestInfo(
          TestRequestId,
          testIsPending,
          testClassName,
          testMethodName,
          testExtraArguments
        )
        val actual = testMethodExitManager.createMethodExitRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
