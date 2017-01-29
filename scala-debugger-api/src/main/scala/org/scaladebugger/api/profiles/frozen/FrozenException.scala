package org.scaladebugger.api.profiles.frozen

/**
 * Represents a generic frozen exception that occurs only on frozen entities.
 */
sealed trait FrozenException extends Exception

/**
 * Represents an exception thrown when trying to invoke a method on
 * a frozen entity.
 */
case object InvocationFrozenException
  extends FrozenException("Method cannot be invoked while frozen!")

/**
 * Represents an exception thrown when trying to access data on a frozen
 * entity that is unavailable.
 */
case object DataUnavailableFrozenException
  extends FrozenException("Data is unavailable while frozen!")

/**
 * Represents an exception thrown when trying to cast to another value which
 * is incorrect for the frozen type.
 */
case object InvalidCastFrozenException
  extends FrozenException("Invalid cast type on frozen entity!")

/**
 * Represents an exception thrown when trying to freeze.
 *
 * @param cause The cause of the exception
 */
case class UnexpectedErrorFrozenException(
  cause: Exception
) extends FrozenException(
  "An unexpected error was encountered when freezing!",
  cause
)
