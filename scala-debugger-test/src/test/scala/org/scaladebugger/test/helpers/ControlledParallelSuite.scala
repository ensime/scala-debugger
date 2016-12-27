package org.scaladebugger.test.helpers

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import ControlledParallelSuite._
import org.scalatest.{Args, Distributor, Status, Suite}

import scala.util.Try

object ControlledParallelSuite {
  lazy val EnvironmentPoolSize: Try[Int] =
    Try(System.getenv("SCALATEST_PARALLEL_TESTS").toInt)
  lazy val DefaultPoolSize: Int = Runtime.getRuntime.availableProcessors() * 2
  def calculatePoolSize(): Int = EnvironmentPoolSize.getOrElse(DefaultPoolSize)

  private val atomicThreadCounter: AtomicInteger = new AtomicInteger

  lazy val threadFactory: ThreadFactory = new ThreadFactory {
    val defaultThreadFactory = Executors.defaultThreadFactory

    def newThread(runnable: Runnable): Thread = {
      val thread = defaultThreadFactory.newThread(runnable)
      thread.setName("ScalaTest-" + atomicThreadCounter.incrementAndGet())
      thread
    }
  }

  import scala.collection.JavaConverters._
  val semaMap: collection.mutable.Map[String, Semaphore] =
    new ConcurrentHashMap[String, Semaphore]().asScala
}

/**
 * Represents a test suite whose pool size can be overridden.
 */
trait ControlledParallelSuite extends Suite {
  def poolSize: Int = calculatePoolSize()

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

  /**
   * Uses a semaphore for synchronization, enabling more than one thread to
   * enter the block at the same time.
   * @param id The id of the block (to distinguish different blocks of code)
   * @param thunk The code to execute
   * @tparam T The return type of the code to execute
   * @return The result of the code execution
   */
  def semaSync[T](id: String)(thunk: => T): T = {
    val semaphore = semaMap.getOrElseUpdate(id, new Semaphore(poolSize))

    semaphore.acquire()
    println(s"==========> Running $id")
    val result = Try(thunk)
    semaphore.release()
    result.get
  }
}
