package org.scaladebugger.api.lowlevel.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.TestModificationWatchpointManager

import scala.util.Success

class ModificationWatchpointManagerSpec extends ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
  private val testModificationWatchpointManager = new TestModificationWatchpointManager(
    mockModificationWatchpointManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ModificationWatchpointManager") {
    describe("#createModificationWatchpointRequest") {
      it("should invoke createModificationWatchpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, testExtraArguments)
          .returning(expected).once()

        val actual = testModificationWatchpointManager.createModificationWatchpointRequest(
          testClassName,
          testFieldName,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createModificationWatchpointRequestFromInfo") {
      it("should invoke createModificationWatchpointRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testClassName = "some.class.name"
        val testFieldName = "someFieldName"
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockModificationWatchpointManager.createModificationWatchpointRequestWithId _)
          .expects(TestRequestId, testClassName, testFieldName, testExtraArguments)
          .returning(expected).once()

        val info = ModificationWatchpointRequestInfo(
          TestRequestId,
          testIsPending,
          testClassName,
          testFieldName,
          testExtraArguments
        )
        val actual = testModificationWatchpointManager.createModificationWatchpointRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
