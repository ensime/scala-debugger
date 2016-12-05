package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import test.TestStepManager

import scala.util.{Try, Success}

class StepManagerSpec extends test.ParallelMockFunSpec
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockStepManager = mock[StepManager]
  private val mockCreateStepRequestWithId = mockFunction[
    String,
    Boolean,
    ThreadReference,
    Int,
    Int,
    Seq[JDIRequestArgument],
    Try[String]
  ]
  private val testStepManager = new TestStepManager(
    mockStepManager
  ) {
    override def createStepRequestWithId(
      requestId: String,
      removeExistingRequests: Boolean,
      threadReference: ThreadReference,
      size: Int,
      depth: Int,
      extraArguments: JDIRequestArgument*
    ): Try[String] = mockCreateStepRequestWithId(
      requestId,
      removeExistingRequests,
      threadReference,
      size,
      depth,
      extraArguments
    )

    override protected def newRequestId(): String = TestRequestId
  }

  describe("StepManager") {
    describe("#createStepRequestWithId") {
      it("should invoke the overloaded method with the flag to remove existing requests set to true") {
        val expected = Success(TestRequestId)
        val testRemoveExistingRequests = true
        val testThreadReference = stub[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        mockCreateStepRequestWithId.expects(
          TestRequestId,
          testRemoveExistingRequests,
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments
        ).returning(expected).once()

        val actual = testStepManager.createStepRequestWithId(
          TestRequestId,
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createStepRequest") {
      it("should invoke createStepRequestWithId") {
        val expected = Success(TestRequestId)
        val testRemoveExistingRequests = true
        val testThreadReference = stub[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        mockCreateStepRequestWithId.expects(
          TestRequestId,
          testRemoveExistingRequests,
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments
        ).returning(expected).once()

        val actual = testStepManager.createStepRequest(
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createStepRequestFromInfo") {
      it("should invoke createStepRequestWithId") {
        val expected = Success(TestRequestId)
        val testIsPending = false
        val testRemoveExistingRequests = false
        val testThreadReference = stub[ThreadReference]
        val testSize = 0
        val testDepth = 1
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        mockCreateStepRequestWithId.expects(
          TestRequestId,
          testRemoveExistingRequests,
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments
        ).returning(expected).once()

        val info = StepRequestInfo(
          TestRequestId,
          testIsPending,
          testRemoveExistingRequests,
          testThreadReference,
          testSize,
          testDepth,
          testExtraArguments
        )
        val actual = testStepManager.createStepRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
