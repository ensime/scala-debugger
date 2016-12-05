package org.scaladebugger.api.pipelines

/**
 * Represents an operation that does nothing (no-op).
 *
 * @tparam A The type of data coming into the operation
 */
class NoOperation[A] extends Operation[A, A] {
  /**
   * Passes the incoming data as the output of this function.
   *
   * @param data The data to process
   *
   * @return The same data
   */
  override def process(data: Seq[A]): Seq[A] = data
}
