package org.scaladebugger.api.utils
import java.io.{InputStream, OutputStream}
import java.util.concurrent.TimeUnit

import scala.util.Try

/**
 * Represents an abstraction on top of a process.
 *
 * @param process The process to provide advanced features on top of
 */
class AdvancedProcess(private val process: Process) extends Process {
  /**
   * Provides the equivalent of the JDK 8 waitFor(timeout, unit).
   *
   * @param timeout The maximum time to wait (no unit)
   * @param unit The time unit associated with the timeout
   * @return True if the subprocess has exited, otherwise false if
   *         reached the timeout
   * @throws InterruptedException If current thread is interrupted
   * @throws IllegalArgumentException If unit is null
   */
  @throws[InterruptedException]
  @throws[IllegalArgumentException]
  def waitForLimit(
    timeout: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS
  ): Boolean = {
    require(unit != null)

    val startTime = System.nanoTime()

    // Calculate time remaining
    val rem = () => unit.toNanos(timeout) - (System.nanoTime() - startTime)

    // While still time remaining, poll the exit value status
    var result: Try[Int] = null
    do {
      result = Try(exitValue())

      // Sleep either 100 milliseconds or the remainder of the available time
      Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem()), 100))
    } while (rem() > 0 && result.isFailure)

    result.isSuccess
  }

  override def exitValue(): Int = process.exitValue()
  override def destroy(): Unit = process.destroy()
  override def waitFor(): Int = process.waitFor()
  override def getOutputStream: OutputStream = process.getOutputStream
  override def getErrorStream: InputStream = process.getErrorStream
  override def getInputStream: InputStream = process.getInputStream
}
