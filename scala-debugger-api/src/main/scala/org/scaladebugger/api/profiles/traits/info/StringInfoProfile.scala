package org.scaladebugger.api.profiles.traits.info

//import acyclic.file

import com.sun.jdi.{ArrayReference, StringReference}
import org.scaladebugger.api.profiles.traits.info.ArrayInfoProfile._

import scala.util.Try


/**
 * Represents the interface for string-based interaction.
 */
trait StringInfoProfile
  extends ObjectInfoProfile with CommonInfoProfile
{
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
