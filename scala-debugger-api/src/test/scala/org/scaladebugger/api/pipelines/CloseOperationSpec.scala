package org.scaladebugger.api.pipelines

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class CloseOperationSpec extends ParallelMockFunSpec {
  describe("CloseOperation") {
    describe("#process") {
      it("should invoke the constructor-provided close function") {
        val mockCloseFunction = mockFunction[Unit]
        val operation = new CloseOperation[Int](mockCloseFunction)

        mockCloseFunction.expects().once()

        operation.process(Seq(1, 2, 3)) should be (empty)
      }
    }
  }
}
