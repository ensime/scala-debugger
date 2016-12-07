package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Mirror
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents common methods between information-gathering profiles.
 */
trait CommonInfo extends JavaInfo {
  /**
   * Returns the Scala virtual machine containing this instance.
   *
   * @return The Scala virtual machine instance
   */
  def scalaVirtualMachine: ScalaVirtualMachine

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  def toJdiInstance: Mirror

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  def toPrettyString: String

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: CommonInfo
}
