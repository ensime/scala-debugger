package org.scaladebugger.api.pipelines

import scala.collection.GenTraversableOnce

/**
 * Represents an operation that maps data to other values in a pipeline.
 *
 * @param flatMapFunc The function to apply
 * @tparam A The type of data coming into the operation
 * @tparam B The type of data going out of the operation
 */
class FlatMapOperation[A, B](
  private val flatMapFunc: (A) => GenTraversableOnce[B]
) extends Operation[A, B] {
  /**
   * Processes incoming data and transforms it into outgoing data.
   *
   * @param data The data to process
   *
   * @return The resulting collection of data
   */
  override def process(data: Seq[A]): Seq[B] = {
    data.flatMap(flatMapFunc)
  }
}
