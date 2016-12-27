package org.scaladebugger.api.lowlevel.requests.filters

import com.sun.jdi.ThreadReference
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ThreadFilterSpec extends ParallelMockFunSpec
{
  private val mockThreadReference = mock[ThreadReference]
  private val threadFilter = ThreadFilter(threadReference = mockThreadReference)

  describe("ThreadFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the thread filter") {
        threadFilter.toProcessor.argument should be (threadFilter)
      }
    }
  }
}
