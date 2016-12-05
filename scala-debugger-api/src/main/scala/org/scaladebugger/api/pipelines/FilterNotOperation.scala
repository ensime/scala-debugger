package org.scaladebugger.api.pipelines

/**
 * Represents an operation that filters data in a pipeline.
 *
 * @param filterNotFunc The function to filter data
 * @tparam A The type of data coming into the operation
 */
class FilterNotOperation[A](
  private val filterNotFunc: (A) => Boolean
) extends Operation[A, A] {
  /**
   * Processes incoming data and filters it into outgoing data.
   *
   * @param data The data to process
   *
   * @return The resulting collection of data
   */
  override def process(data: Seq[A]): Seq[A] = {
    data.filterNot(filterNotFunc)
  }
}
