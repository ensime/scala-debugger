package org.scaladebugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ForeachOperationSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
  describe("ForeachOperation") {
    describe("#process") {
      it("should apply the operation's function to each data element") {
        val expected = Seq(1, 2, 3)
        var actual: Seq[Int] = Nil

        val operation = new ForeachOperation[Int](i => actual :+= i)
        operation.process(expected)

        actual should be (expected)
      }

      it("should return an empty collection") {
        val data = Seq(1, 2, 3)
        val operation = new ForeachOperation[Int](i => s"${i}string")
        val actual = operation.process(data)

        actual should be (empty)
      }
    }
  }
}
