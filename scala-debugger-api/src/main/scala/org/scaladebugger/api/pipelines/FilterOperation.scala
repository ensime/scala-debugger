package org.scaladebugger.api.pipelines

/**
 * Represents an operation that filters data in a pipeline.
 *
 * @param filterFunc The function to filter data
 * @tparam A The type of data coming into the operation
 */
class FilterOperation[A](
  private val filterFunc: (A) => Boolean
) extends Operation[A, A] {
  /**
   * Processes incoming data and filters it into outgoing data.
   *
   * @param data The data to process
   *
   * @return The resulting collection of data
   */
  override def process(data: Seq[A]): Seq[A] = {
    data.filter(filterFunc)
  }
}
