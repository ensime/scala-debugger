package org.scaladebugger.api.profiles.pure.info


import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of an array profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param _stringReference The reference to the underlying JDI string
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _referenceType The reference type for this array
 */
class PureStringInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _stringReference: StringReference
)(
  override protected val _virtualMachine: VirtualMachine = _stringReference.virtualMachine(),
  private val _referenceType: ReferenceType = _stringReference.referenceType()
) extends PureObjectInfo(scalaVirtualMachine, infoProducer, _stringReference)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with StringInfo {
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
  override def toJavaInfo: StringInfo = {
    infoProducer.toJavaInfo.newStringInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      stringReference = _stringReference
    )(
      virtualMachine = _virtualMachine,
      referenceType = _referenceType
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StringReference = _stringReference
}
