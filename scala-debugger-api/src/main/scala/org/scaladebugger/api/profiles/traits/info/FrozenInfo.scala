package org.scaladebugger.api.profiles.traits.info

import java.io.Serializable

/**
 * Represents a profile that provides common methods to freeze information
 * such that it can be persisted and processed elsewhere.
 */
trait FrozenInfo {
  /**
   * Returns whether or not this information is frozen.
   *
   * @return If true, this information has been frozen, otherwise it
   *         represents live data
   */
  def isFrozen: Boolean

  /**
   * Freezes the information, collecting all data at the current point in time.
   *
   * @return The information frozen at the current point in time
   */
  def freeze(): _ <: Serializable
}
