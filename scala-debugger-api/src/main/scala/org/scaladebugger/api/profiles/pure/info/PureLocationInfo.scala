package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{Location, Method, ReferenceType}
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, LocationInfo, MethodInfo, ReferenceTypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a location profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            location
 * @param infoProducer The producer of info-based profile instances
 * @param _location The reference to the underlying JDI location
 */
class PureLocationInfo(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducer,
  private val _location: Location
) extends LocationInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: LocationInfo = {
    infoProducer.toJavaInfo.newLocationInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      location = _location
    )
  }

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
  override def declaringType: ReferenceTypeInfo =
    newReferenceTypeProfile(_location.declaringType())

  /**
   * Retrieves the method information for the method containing this location.
   *
   * @return The method information
   */
  override def method: MethodInfo =
    newMethodProfile(_location.method())

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfo = infoProducer.newReferenceTypeInfo(
    scalaVirtualMachine,
    referenceType
  )

  protected def newMethodProfile(method: Method): MethodInfo =
    infoProducer.newMethodInfo(scalaVirtualMachine, method)
}
