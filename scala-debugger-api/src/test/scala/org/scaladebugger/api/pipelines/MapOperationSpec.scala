package org.scaladebugger.api.pipelines

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class MapOperationSpec extends ParallelMockFunSpec {
  describe("MapOperation") {
    describe("#process") {
      it("should map all elements to other values") {
        val expected = Seq("1string", "2string", "3string")

        val data = Seq(1, 2, 3)
        val operation = new MapOperation[Int, String](i => s"${i}string")
        val actual = operation.process(data)

        actual should be (expected)
      }
    }
  }
}
