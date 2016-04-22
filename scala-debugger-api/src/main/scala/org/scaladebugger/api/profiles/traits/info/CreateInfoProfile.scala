package org.scaladebugger.api.profiles.traits.info

import scala.util.Try
//import acyclic.file

/**
 * Represents the interface that needs to be implemented to provide
 * ability to create data using a specific debug profile.
 */
trait CreateInfoProfile {
  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  def createRemotely(value: AnyVal): ValueInfoProfile

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return Success containing the information about the remote value,
   *         otherwise a failure
   */
  def tryCreateRemotely(value: AnyVal): Try[ValueInfoProfile] =
    Try(createRemotely(value))

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  def createRemotely(value: String): ValueInfoProfile

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return Success containing the information about the remote value,
   *         otherwise a failure
   */
  def tryCreateRemotely(value: String): Try[ValueInfoProfile] =
    Try(createRemotely(value))
}
