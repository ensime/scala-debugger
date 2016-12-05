package org.scaladebugger.api.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, FunSpec, Matchers}

class MemoizationSpec extends test.ParallelMockFunSpec
{
  describe("Memoization") {
    describe("#apply") {
      it("should execute and return the result of the memo function if the input is new") {
        val mockTestFunction = mockFunction[Seq[Int], Int, String]

        val m = new Memoization[(Seq[Int], Int), (Seq[Int], Int), String](
          (t: (Seq[Int], Int)) => mockTestFunction(t._1, t._2)
        )

        val input = Seq(1, 2, 3)
        val index = 2
        val output = input(index).toString

        val totalRuns = 10

        mockTestFunction.expects(input, index).returning(output).once()

        for (i <- 1 to totalRuns) {
          m((input, index)) should be (output)
        }
      }

      it("should return a cached copy of the memo function if the input is old") {
        val mockTestFunction = mockFunction[Seq[Int], Int, String]

        val m = new Memoization[(Seq[Int], Int), (Seq[Int], Int), String](
          (t: (Seq[Int], Int)) => mockTestFunction(t._1, t._2)
        )

        val input = Seq(1, 2, 3)

        for (i <- 0 to input.length - 1) {
          mockTestFunction.expects(input, i).returning(input(i).toString).once()
        }

        // Should only call the memo function once per unique input,
        // yet return the same value
        for (i <- 0 to (input.length - 1) * 5) {
          val index = i % input.length
          val output = input(index).toString
          m((input, index)) should be (output)
        }
      }

      it("should execute and return the result of the memo function if the input is old but the cache element is invalidated") {
        val mockTestFunction = mockFunction[Seq[Int], Int, String]

        // Mark cache as always invalid, so always rerun the function
        val m = new Memoization[(Seq[Int], Int), (Seq[Int], Int), String](
          (t: (Seq[Int], Int)) => mockTestFunction(t._1, t._2),
          cacheInvalidFunc = (t: (Seq[Int], Int)) => true
        )

        val input = Seq(1, 2, 3)
        val index = 2
        val output = input(index).toString

        val totalRuns = 10

        mockTestFunction.expects(input, index)
          .returning(output).repeated(totalRuns).times()

        for (i <- 1 to totalRuns) {
          m((input, index)) should be (output)
        }
      }

      it("should convert the input to the cache key using the provided key function") {
        val mockTestFunction = mockFunction[Seq[Int], Int, String]
        val cache = collection.mutable.Map.empty[Int, String]

        // Used to convert input to cache key value
        import scala.language.implicitConversions
        implicit def encode(input: (Seq[Int], Int)): Int = input._2

        val m = new Memoization[(Seq[Int], Int), Int, String](
          (t: (Seq[Int], Int)) => mockTestFunction(t._1, t._2),
          cache = cache
        )

        val input = Seq(1, 2, 3)
        val index = 2

        mockTestFunction.expects(input, index).returning("").once()

        m((input, index))

        cache.keySet should contain only (index)
      }
    }

    describe("type alias ==>") {
      it("should use the input as the key to the cache") {
        import Memoization.==>

        val mockTestFunction = mockFunction[Seq[Int], Int, String]
        val cache = collection.mutable.Map.empty[(Seq[Int], Int), String]

        val m: (Seq[Int], Int) ==> String = new Memoization(
          (t: (Seq[Int], Int)) => mockTestFunction(t._1, t._2),
          cache = cache
        )

        val input = Seq(1, 2, 3)
        val index = 2

        mockTestFunction.expects(input, index).returning("").once()

        m((input, index))

        cache.keySet should contain only ((input, index))
      }
    }
  }
}
