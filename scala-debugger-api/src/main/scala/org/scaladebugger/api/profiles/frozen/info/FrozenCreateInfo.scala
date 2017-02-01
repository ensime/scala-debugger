package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.{FrozenException, InvocationFrozenException}
import org.scaladebugger.api.profiles.traits.info.{CreateInfo, ValueInfo}

/**
 * Represents a frozen implementation of the create info trait.
 */
trait FrozenCreateInfo extends CreateInfo {
  /** Unavailable on frozen entity. */
  @throws[FrozenException]
  override def createRemotely(value: AnyVal): ValueInfo =
    throw InvocationFrozenException

  /** Unavailable on frozen entity. */
  @throws[FrozenException]
  override def createRemotely(value: String): ValueInfo =
    throw InvocationFrozenException
}
