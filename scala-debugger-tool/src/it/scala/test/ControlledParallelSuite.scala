package test

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

import org.scalatest.{Args, Distributor, Status, Suite}

import scala.util.Try

import ControlledParallelSuite._

object ControlledParallelSuite {
  lazy val EnvironmentPoolSize: Try[Int] =
    Try(System.getenv("SCALATEST_PARALLEL_TESTS").toInt)
  lazy val DefaultPoolSize: Int = Runtime.getRuntime.availableProcessors() * 2
}

/**
 * Represents a test suite whose pool size can be overridden.
 */
trait ControlledParallelSuite extends Suite {
  private val atomicThreadCounter: AtomicInteger = new AtomicInteger

  protected val threadFactory: ThreadFactory = new ThreadFactory {
    val defaultThreadFactory = Executors.defaultThreadFactory

    def newThread(runnable: Runnable): Thread = {
      val thread = defaultThreadFactory.newThread(runnable)
      thread.setName("ScalaTest-" + atomicThreadCounter.incrementAndGet())
      thread
    }
  }

  protected def poolSize: Int = EnvironmentPoolSize.getOrElse(DefaultPoolSize)

  protected def newExecutorService(
    poolSize: Int,
    threadFactory: ThreadFactory
  ): ExecutorService = {
    Executors.newFixedThreadPool(poolSize, threadFactory)
  }

  protected def newConcurrentDistributor(args: Args, execSvc: ExecutorService): Distributor = {
    val concurrentDsitributorClass = Class.forName("org.scalatest.tools.ConcurrentDistributor")
    val constructor = concurrentDsitributorClass.getConstructor(classOf[Args], classOf[ExecutorService])
    constructor.setAccessible(true)
    constructor.newInstance(args, execSvc).asInstanceOf[Distributor]
  }

  override def run(testName: Option[String], args: Args): Status = {
    super.run(
      testName,
      args.copy(distributor = Some(
        newConcurrentDistributor(args, newExecutorService(poolSize, threadFactory)))
      )
    )
  }
}
