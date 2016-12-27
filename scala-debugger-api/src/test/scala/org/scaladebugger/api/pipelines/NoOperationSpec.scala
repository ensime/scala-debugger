package org.scaladebugger.api.pipelines

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class NoOperationSpec extends ParallelMockFunSpec {
  describe("NoOperation") {
    describe("#process") {
      it("should return the input as the output") {
        val expected = Seq(1, 2, 3)

        val operation = new NoOperation[Int]
        val actual = operation.process(expected)

        actual should be (expected)
      }
    }
  }
}
