package org.scaladebugger.api.lowlevel.requests.filters

import com.sun.jdi.ObjectReference
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class InstanceFilterSpec extends ParallelMockFunSpec
{
  private val mockObjectReference = mock[ObjectReference]
  private val instanceFilter = InstanceFilter(
    objectReference = mockObjectReference
  )

  describe("InstanceFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the instance filter") {
        instanceFilter.toProcessor.argument should be (instanceFilter)
      }
    }
  }
}
