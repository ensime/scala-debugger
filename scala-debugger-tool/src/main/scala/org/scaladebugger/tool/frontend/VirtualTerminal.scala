package org.scaladebugger.tool.frontend

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, TimeUnit}

import org.scaladebugger.tool.frontend.VirtualTerminal._

object VirtualTerminal {
  /** Maximum queue size for input data. */
  val DefaultMaxInputQueueSize: Int = 200

  /** Maximum queue size for output data. */
  val DefaultMaxOutputQueueSize: Int = 200

  /** Maximum time to wait for new input before terminating. */
  val DefaultWaitTime: Long = 2000
}

/**
 * Represents a terminal that reads from input provided on a queue
 * and outputs results to a different queue.
 *
 * @param inputQueue The queue used for new input to the terminal
 * @param outputQueue The queue used for new output from the terminal
 * @param waitTime The maximum time to wait for retrieving input as well
 *                 as wait to add new input (in milliseconds); if negative,
 *                 will wait indefinitely
 */
class VirtualTerminal(
  private val inputQueue: BlockingQueue[String] =
    new ArrayBlockingQueue[String](DefaultMaxInputQueueSize),
  private val outputQueue: BlockingQueue[String] =
    new ArrayBlockingQueue[String](DefaultMaxOutputQueueSize),
  private val waitTime: Long = DefaultWaitTime
) extends Terminal {
  /**
   * Retrieves the next line of output from the terminal.
   *
   * @param waitTime The maximum time to wait for new output in milliseconds,
   *                 or 0 if waiting indefinitely
   * @return Some new line of output from the terminal if output exists,
   *         otherwise None
   */
  def nextOutputLine(waitTime: Long = 0): Option[String] = {
    if (waitTime > 0) Option(outputQueue.poll(waitTime, TimeUnit.MILLISECONDS))
    else Option(outputQueue.take())
  }

  /**
   * Adds a new line of input to be fed into the terminal.
   *
   * @param text The new line of text
   * @throws IllegalStateException When maximum output queue capacity has been
   *                               reached and is full for longer than the
   *                               maximum wait time
   */
  @throws[IllegalStateException]
  def newInputLine(text: String): Unit = {
    val result = inputQueue.offer(text, waitTime, TimeUnit.MILLISECONDS)
    if (!result) throw new IllegalStateException(s"Unable to add $text")
  }

  /**
   * Reads the next line from the terminal.
   *
   * @return Some line if found, otherwise None if the maximum time allowed
   *         for new input has been exceeded
   */
  override def readLine(): Option[String] = {
    if (waitTime < 0) Option(inputQueue.take())
    else Option(inputQueue.poll(waitTime, TimeUnit.MILLISECONDS))
  }

  /**
   * Writes the provided text to the terminal.
   *
   * @param text The text to write out to the terminal
   * @throws IllegalStateException When the output queue's capacity has been
   *                               reached
   */
  @throws[IllegalStateException]
  override def write(text: String): Unit = {
    outputQueue.add(text)
  }
}
