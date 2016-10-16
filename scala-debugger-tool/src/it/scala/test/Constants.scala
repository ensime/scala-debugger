package test

import org.scalatest.Tag
import org.scalatest.time.{Milliseconds, Seconds, Span}

/**
 * Constants for our tests.
 */
object Constants {
  val EventuallyTimeout = Span(10, Seconds)
  val EventuallyInterval = Span(5, Milliseconds)
  val NextOutputLineTimeout = Span(5, Seconds)
  val NoWindows = Tag("NoWindows")
}
