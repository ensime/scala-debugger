package test

import org.scaladebugger.api.utils.Logging

/**
 * Contains helper methods for testing.
 */
trait TestUtilities { this: Logging =>
  /**
   * Executes the block of code and logs the time taken to evaluate it.
   *
   * @param block The block of code to execute
   * @tparam T The return type of the block of code
   *
   * @return The value returned from the block of code
   */
  def logTimeTaken[T](block: => T): T = {
    val startTime = System.currentTimeMillis()

    try {
      block
    } finally {
      val finalTime = System.currentTimeMillis() - startTime
      logger.info(s"Time taken: ${finalTime / 1000.0}s")
    }
  }
}
