package org.scaladebugger.api.pipelines

/**
 * Represents an operation that maps data to other values in a pipeline.
 *
 * @param mapFunc The function to map data to outgoing data
 * @tparam A The type of data coming into the operation
 * @tparam B The type of data going out of the operation
 */
class MapOperation[A, B](
  private val mapFunc: (A) => B
) extends Operation[A, B] {
  /**
   * Processes incoming data and transforms it into outgoing data.
   *
   * @param data The data to process
   *
   * @return The resulting collection of data
   */
  override def process(data: Seq[A]): Seq[B] = {
    data.map(mapFunc)
  }
}
