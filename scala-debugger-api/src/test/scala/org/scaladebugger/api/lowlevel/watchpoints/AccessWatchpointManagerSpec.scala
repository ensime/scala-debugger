package org.scaladebugger.api.lowlevel.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.TestAccessWatchpointManager

import scala.util.Success

class AccessWatchpointManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockAccessWatchpointManager = mock[AccessWatchpointManager]
  private val testAccessWatchpointManager = new TestAccessWatchpointManager(
    mockAccessWatchpointManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("AccessWatchpointManager") {
    describe("#createAccessWatchpointRequest") {
      it("should invoke createAccessWatchpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, testExtraArguments)
          .returning(expected).once()

        val actual = testAccessWatchpointManager.createAccessWatchpointRequest(
          testClassName,
          testFieldName,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createAccessWatchpointRequestFromInfo") {
      it("should invoke createAccessWatchpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, testExtraArguments)
          .returning(expected).once()

        val info = AccessWatchpointRequestInfo(
          TestRequestId,
          testIsPending,
          testClassName,
          testFieldName,
          testExtraArguments
        )
        val actual = testAccessWatchpointManager.createAccessWatchpointRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
