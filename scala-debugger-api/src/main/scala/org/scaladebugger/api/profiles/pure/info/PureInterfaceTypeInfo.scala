package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.{InterfaceTypeInfo, ValueInfo, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a interface type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            interface type
 * @param infoProducer The producer of info-based profile instances
 * @param _interfaceType The underlying JDI interface type to wrap
 */
class PureInterfaceTypeInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _interfaceType: InterfaceType
) extends PureReferenceTypeInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _referenceType = _interfaceType
) with InterfaceTypeInfo {
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
  override def toJavaInfo: InterfaceTypeInfo = {
    infoProducer.toJavaInfo.newInterfaceTypeInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      interfaceType = _interfaceType
    )
  }

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
  override def implementors: Seq[ClassTypeInfo] = {
    import scala.collection.JavaConverters._
    _interfaceType.implementors().asScala.map(newClassTypeProfile)
  }

  /**
   * Returns the prepared interfaces which directly extend this interface.
   *
   * @return The collection of interface type info profiles
   */
  override def subinterfaces: Seq[InterfaceTypeInfo] = {
    import scala.collection.JavaConverters._
    _interfaceType.subinterfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns the interfaces directly extended by this interface.
   *
   * @return The collection of interface type info profiles
   */
  override def superinterfaces: Seq[InterfaceTypeInfo] = {
    import scala.collection.JavaConverters._
    _interfaceType.superinterfaces().asScala.map(newInterfaceTypeProfile)
  }
}
