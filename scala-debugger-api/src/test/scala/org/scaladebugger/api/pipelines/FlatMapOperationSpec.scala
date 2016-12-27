package org.scaladebugger.api.pipelines

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class FlatMapOperationSpec extends ParallelMockFunSpec {
  describe("FlatMapOperation") {
    describe("#process") {
      it("should map all elements to other values and then flatten them") {
        val expected = Seq(1, 2, 2, 3, 3, 4, 4, 5, 5, 6)

        val data = Seq(1, 2, 3, 4, 5)
        val operation = new FlatMapOperation[Int, Int](i => Seq(i, i + 1))
        val actual = operation.process(data)

        actual should be (expected)
      }
    }
  }
}
