package org.scaladebugger.api.utils

import scala.util.Try

/**
 * Contains helpful utility implicits.
 */
object Implicits {
  /** Provides utility methods to the try class. */
  implicit class TryWrapper[T](tryInstance: Try[T]) {
    /**
     * Converts the try instance into an either of [throwable, value].
     *
     * @return The either representing the try instance
     */
    def toEither: Either[Throwable, T] = Either.cond(
      tryInstance.isSuccess,
      tryInstance.get,
      tryInstance.failed.get
    )

    /**
     * Converts the try instance into an either of [throwable, value].
     *
     * @param throwableClass The specific class of throwable the either should
     *                       represent
     * @tparam TClass The throwable type parameter
     * @return The either representing the try instance
     */
    def toEither[TClass <: Throwable](
      throwableClass: Class[TClass]
    ): Either[TClass, T] = toEither.asInstanceOf[Either[TClass, T]]
  }
}
