package org.scaladebugger.api.profiles.traits.info


import com.sun.jdi.{ArrayReference, StringReference}
import org.scaladebugger.api.profiles.traits.info.ArrayInfo._

import scala.util.Try


/**
 * Represents the interface for string-based interaction.
 */
trait StringInfo extends ObjectInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: StringInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
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
