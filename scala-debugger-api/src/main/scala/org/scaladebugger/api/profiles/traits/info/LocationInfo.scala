package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Location
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents the interface for location-based interaction.
 */
//@Freezable
trait LocationInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: LocationInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: Location

  /**
   * Retrieves the reference type information for the type containing this
   * location.
   *
   * @return The reference type information
   */
  @CanFreeze(ReturnType.FreezeObject)
  def declaringType: ReferenceTypeInfo

  /**
   * Retrieves the method information for the method containing this location.
   *
   * @return The method information
   */
  @CanFreeze(ReturnType.FreezeObject)
  def method: MethodInfo

  /**
   * Retrieves the code position within the location's method.
   *
   * @return The code position, or -1 if not available
   */
  @CanFreeze
  def codeIndex: Long

  /**
   * Retrieves the code position within the location's method.
   *
   * @return Some code position, or None if not available
   */
  def codeIndexOption: Option[Long] = Option(codeIndex).filter(_ >= 0)

  /**
   * Retrieves the line number associated with the location.
   *
   * @return The line number, or -1 if not available
   */
  @CanFreeze
  def lineNumber: Int

  /**
   * Retrieves the line number associated with the location.
   *
   * @return Some line number, or None if not available
   */
  def lineNumberOption: Option[Int] = Option(lineNumber).filter(_ >= 0)

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return The identifying name
   */
  @CanFreeze
  def sourceName: String

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return Success containing the identifying name, otherwise a failure
   */
  def trySourceName: Try[String] = Try(sourceName)

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return The source path
   */
  @CanFreeze
  def sourcePath: String

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return Success containing the source path, otherwise a failure
   */
  def trySourcePath: Try[String] = Try(sourcePath)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    trySourcePath.getOrElse("???") + " : " + lineNumber
  }
}
