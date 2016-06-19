package org.scaladebugger.test.info

import org.scaladebugger.test.helpers.Stubs._

/**
 * Provides test of finding threads.
 *
 * @note Should have a class name of org.scaladebugger.test.info.Threads
 */
object Threads {
  def main(args: Array[String]) {
    val infiniteRun = new Runnable {
      override def run(): Unit = while (true) Thread.sleep(1000)
    }

    val threadGroup1 = new ThreadGroup("test1")
    val threadGroup2 = new ThreadGroup("test2")

    new Thread(infiniteRun, "thread-without-group").start()
    new Thread(threadGroup1, infiniteRun, "test-thread").start()
    new Thread(threadGroup2, infiniteRun, "test-thread").start()
    new Thread(threadGroup1, infiniteRun, "unique-test-name").start()

    noop(None)
  }
}

