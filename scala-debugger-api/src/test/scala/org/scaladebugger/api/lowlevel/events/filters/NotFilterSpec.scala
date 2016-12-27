package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class NotFilterSpec extends ParallelMockFunSpec
{
  private val notFilter = NotFilter(mock[JDIEventFilter])

  describe("NotFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the or filter") {
        notFilter.toProcessor.argument should be (notFilter)
      }
    }
  }
}
