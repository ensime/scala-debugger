package org.scaladebugger.api.lowlevel.methods
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestMethodEntryManager

import scala.util.Success

class MethodEntryManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMethodEntryManager = mock[MethodEntryManager]
  private val testMethodEntryManager = new TestMethodEntryManager(
    mockMethodEntryManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MethodEntryManager") {
    describe("#createMethodEntryRequest") {
      it("should invoke createMethodEntryRequestWithId") {
        val expected = Success(TestRequestId)
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMethodEntryManager.createMethodEntryRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, testExtraArguments)
          .returning(expected).once()

        val actual = testMethodEntryManager.createMethodEntryRequest(
          testClassName,
          testMethodName,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createMethodEntryRequestFromInfo") {
      it("should invoke createMethodEntryRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testClassName = "some.class.name"
        val testMethodName = "someMethodName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockMethodEntryManager.createMethodEntryRequestWithId _)
          .expects(TestRequestId, testClassName, testMethodName, testExtraArguments)
          .returning(expected).once()

        val info = MethodEntryRequestInfo(
          TestRequestId,
          testIsPending,
          testClassName,
          testMethodName,
          testExtraArguments
        )
        val actual = testMethodEntryManager.createMethodEntryRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
