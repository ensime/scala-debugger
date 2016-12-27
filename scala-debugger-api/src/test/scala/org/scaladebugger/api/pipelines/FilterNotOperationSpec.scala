package org.scaladebugger.api.pipelines

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class FilterNotOperationSpec extends ParallelMockFunSpec {
  describe("FilterNotOperation") {
    describe("#process") {
      it("should filter data to include only predicates resolving to false") {
        val expected = Seq(1, 3, 5)

        val data = Seq(1, 2, 3, 4, 5)
        val operation = new FilterNotOperation[Int](_ % 2 == 0)
        val actual = operation.process(data)

        actual should be (expected)
      }
    }
  }
}
