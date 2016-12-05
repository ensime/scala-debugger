package org.scaladebugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag

class NoOperationSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
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
