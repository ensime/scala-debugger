package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.traits.info.FrozenInfo

/**
 * Represents a frozen implementation of the frozen info trait.
 */
trait FrozenFrozenInfo extends FrozenInfo {
  /**
   * Returns whether or not this information is frozen.
   *
   * @return If true, this information has been frozen, otherwise it
   *         represents live data
   */
  override def isFrozen: Boolean = true
}
