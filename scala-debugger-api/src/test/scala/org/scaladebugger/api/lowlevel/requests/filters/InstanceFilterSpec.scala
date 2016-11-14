package org.scaladebugger.api.lowlevel.requests.filters
import acyclic.file

import com.sun.jdi.ObjectReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class InstanceFilterSpec extends test.ParallelMockFunSpec
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
