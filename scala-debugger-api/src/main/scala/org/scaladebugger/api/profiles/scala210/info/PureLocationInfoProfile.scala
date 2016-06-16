package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.{Location, Method, ReferenceType}
import org.scaladebugger.api.profiles.traits.info.{LocationInfoProfile, MethodInfoProfile, ReferenceTypeInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a location profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            location
 * @param _location The reference to the underlying JDI location
 */
class PureLocationInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val _location: Location
) extends LocationInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Location = _location

  /**
   * Retrieves the code position within the location's method.
   *
   * @return The code position, or -1 if not available
   */
  override def codeIndex: Long = _location.codeIndex()

  /**
   * Retrieves the line number associated with the location.
   *
   * @return The line number, or -1 if not available
   */
  override def lineNumber: Int = _location.lineNumber()

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return The identifying name
   */
  override def sourceName: String = _location.sourceName()

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return The source path
   */
  override def sourcePath: String = _location.sourcePath()

  /**
   * Retrieves the reference type information for the type containing this
   * location.
   *
   * @return The reference type information
   */
  override def declaringType: ReferenceTypeInfoProfile =
    newReferenceTypeProfile(_location.declaringType())

  /**
   * Retrieves the method information for the method containing this location.
   *
   * @return The method information
   */
  override def method: MethodInfoProfile =
    newMethodProfile(_location.method())

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(scalaVirtualMachine, method)
}
