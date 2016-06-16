package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{InterfaceTypeInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a interface type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            interface type
 * @param _interfaceType The underlying JDI interface type to wrap
 */
class PureInterfaceTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _interfaceType: InterfaceType
) extends PureReferenceTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _referenceType = _interfaceType
) with InterfaceTypeInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: InterfaceType = _interfaceType

  /**
   * Returns the prepared classes which directly implement this interface.
   *
   * @return The collection of class type info profiles
   */
  override def implementors: Seq[ClassTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _interfaceType.implementors().asScala.map(newClassTypeProfile)
  }

  /**
   * Returns the prepared interfaces which directly extend this interface.
   *
   * @return The collection of interface type info profiles
   */
  override def subinterfaces: Seq[InterfaceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _interfaceType.subinterfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns the interfaces directly extended by this interface.
   *
   * @return The collection of interface type info profiles
   */
  override def superinterfaces: Seq[InterfaceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _interfaceType.superinterfaces().asScala.map(newInterfaceTypeProfile)
  }
}
