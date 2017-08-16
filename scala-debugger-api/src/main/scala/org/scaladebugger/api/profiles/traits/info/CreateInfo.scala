package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.macros.freeze.{CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * ability to create data using a specific debug profile.
 */
//@Freezable
trait CreateInfo {
  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  @CannotFreeze
  def createRemotely(value: AnyVal): ValueInfo

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return Success containing the information about the remote value,
   *         otherwise a failure
   */
  def tryCreateRemotely(value: AnyVal): Try[ValueInfo] =
    Try(createRemotely(value))

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  @CannotFreeze
  def createRemotely(value: String): ValueInfo

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return Success containing the information about the remote value,
   *         otherwise a failure
   */
  def tryCreateRemotely(value: String): Try[ValueInfo] =
    Try(createRemotely(value))
}
