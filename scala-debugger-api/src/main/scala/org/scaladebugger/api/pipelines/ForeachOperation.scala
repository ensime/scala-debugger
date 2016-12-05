package org.scaladebugger.api.pipelines

/**
 * Represents an operation that maps data to other values in a pipeline.
 *
 * @param foreachFunc The function to apply
 * @tparam A The type of data coming into the operation
 */
class ForeachOperation[A](
  private val foreachFunc: (A) => Unit
) extends Operation[A, Unit] {
  /**
   * Processes incoming data.
   *
   * @param data The data to process
   *
   * @return Unused (empty collection)
   */
  override def process(data: Seq[A]): Seq[Unit] = {
    data.foreach(foreachFunc)
    Nil
  }
}
