package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.StringReference
import org.scaladebugger.macros.freeze.{CannotFreeze, Freezable}

/**
 * Represents the interface for string-based interaction.
 */
@Freezable
trait StringInfo extends ObjectInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: StringInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: StringReference

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val q = '"'
    s"$q$toLocalValue$q"
  }
}
