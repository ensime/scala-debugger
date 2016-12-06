package test

import scala.concurrent.Future

/**
 * Contains workarounds implemented in Scala 2.12.
 */
object Workarounds {
  /**
   * Resolves https://github.com/scalatest/scalatest/issues/1025
   * For Scala 2.10/2.11, just invokes `future.failed`.
   */
  def transformFutureToFailed[T](future: Future[T]): Future[Throwable] =
    future.failed
}
