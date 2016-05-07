package org.scaladebugger.api.lowlevel.exceptions
import acyclic.file

import com.sun.jdi.request.{EventRequest, EventRequestManager, ExceptionRequest}
import com.sun.jdi.{ReferenceType, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.DummyOperationException

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

class DummyExceptionManagerSpec extends FunSpec with Matchers with MockFactory
  with ParallelTestExecution with org.scalamock.matchers.Matchers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val exceptionManager = new DummyExceptionManager

  describe("DummyExceptionManager") {
    describe("#createCatchallExceptionRequestWithId") {
      it("should return a failure of dummy operation") {
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val result = exceptionManager.createCatchallExceptionRequestWithId(
          TestRequestId,
          testNotifyCaught,
          testNotifyUncaught
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#exceptionRequestListById") {
      it("should return an empty list") {
        exceptionManager.exceptionRequestListById should be (empty)
      }
    }

    describe("#exceptionRequestList") {
      it("should return an empty list") {
        exceptionManager.exceptionRequestList should be (empty)
      }
    }

    describe("#createExceptionRequestWithId") {
      it("should return a failure of dummy operation") {
        val testClassName = "some.class.name"
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val result = exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testClassName,
          testNotifyCaught,
          testNotifyUncaught
        )

        result.isFailure should be (true)
        result.failed.get shouldBe a [DummyOperationException]
      }
    }

    describe("#hasExceptionRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = exceptionManager.hasExceptionRequestWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#hasExceptionRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"

        val actual = exceptionManager.hasExceptionRequest(testClassName)

        actual should be (expected)
      }
    }

    describe("#getExceptionRequestInfoWithId") {
      it("should return None") {
        val expected = None

        val actual = exceptionManager.getExceptionRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#getExceptionRequestWithId") {
      it("should return None") {
        val expected = None

        val actual = exceptionManager.getExceptionRequestWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#getExceptionRequest") {
      it("should return None") {
        val expected = None
        val testClassName = "some.class.name"

        val actual = exceptionManager.getExceptionRequest(testClassName)

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequestWithId") {
      it("should return false") {
        val expected = false

        val actual = exceptionManager.removeExceptionRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeExceptionRequest") {
      it("should return false") {
        val expected = false
        val testClassName = "some.class.name"

        val actual = exceptionManager.removeExceptionRequest(testClassName)

        actual should be (expected)
      }
    }
  }
}
