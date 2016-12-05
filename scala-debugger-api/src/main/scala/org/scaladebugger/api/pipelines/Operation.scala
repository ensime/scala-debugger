package org.scaladebugger.api.pipelines

/**
 * Represents a single operation to be applied to data in a pipeline.
 *
 * @tparam A The incoming data type
 * @tparam B The outgoing data type
 */
trait Operation[A, B] {
  /**
   * Processes incoming data and transforms it into outgoing data.
   *
   * @param data The data to process
   *
   * @return The resulting collection of data
   */
  def process(data: Seq[A]): Seq[B]
}
