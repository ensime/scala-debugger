package test

import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

/**
 * Represents a test suite whose pool size is fixed across all
 * specs/suites that inherit this suite.
 */
trait FixedParallelSuite extends ControlledParallelSuite {
  protected lazy val executorService =
    Executors.newFixedThreadPool(poolSize, threadFactory)

  override protected def newExecutorService(
    poolSize: Int,
    threadFactory: ThreadFactory
  ): ExecutorService = {
    executorService
  }
}
