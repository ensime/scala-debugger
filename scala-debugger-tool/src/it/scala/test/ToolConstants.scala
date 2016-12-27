package test

import org.scalatest.Tag
import org.scalatest.time.{Milliseconds, Seconds, Span}

/**
 * ToolConstants for our tests.
 */
object ToolConstants {
  val DefaultMaxInputQueueSize = 50000
  val DefaultMaxOutputQueueSize = 50000
  val AccumulationTimeout = Span(500, Milliseconds)
  val EventuallyTimeout = Span(10, Seconds)
  val EventuallyInterval = Span(5, Milliseconds)
  val NewInputLineTimeout = Span(10, Seconds)
  val NextOutputLineTimeout = Span(5, Seconds)
  val NoWindows = Tag("NoWindows")
}
