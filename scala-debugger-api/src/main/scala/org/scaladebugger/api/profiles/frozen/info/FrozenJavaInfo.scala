package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.InvocationFrozenException
import org.scaladebugger.api.profiles.traits.info.JavaInfo

/**
 * Represents a frozen implementation of the Java info trait.
 */
trait FrozenJavaInfo extends JavaInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = false

  /** Unavailable on frozen entity. */
  @throws[InvocationFrozenException]
  override def toJavaInfo: AnyRef =
    throw new InvocationFrozenException("toJavaInfo")
}
