package test

import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

import FixedParallelSuite._

object FixedParallelSuite {
  lazy val DefaultExecutorService = Executors.newFixedThreadPool(
    ControlledParallelSuite.calculatePoolSize(),
    ControlledParallelSuite.threadFactory
  )
}

/**
 * Represents a test suite whose pool size is fixed across all
 * specs/suites that inherit this suite.
 */
trait FixedParallelSuite extends ControlledParallelSuite {
  protected lazy val executorService = DefaultExecutorService

  override protected def newExecutorService(
    poolSize: Int,
    threadFactory: ThreadFactory
  ): ExecutorService = {
    executorService
  }
}
