package org.scaladebugger.api.pipelines

/**
 * Represents an operation that closes the pipeline.
 *
 * @tparam A The type of data coming into the operation
 * @param closeFunc The function to be executed by the close operation
 */
class CloseOperation[A](
  private val closeFunc: () => Unit
) extends Operation[A, Unit] {
  /**
   * Ignores the incoming data and closes the pipeline.
   *
   * @param data The data to process
   *
   * @return Unused (empty collection)
   */
  override def process(data: Seq[A]): Seq[Unit] = {
    closeFunc()
    Nil
  }
}
