package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a class object profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class object
 * @param _classObjectReference The reference to the underlying JDI class object
 * @param _infoProducer The producer of info-based profile instances
 * @param _virtualMachine The virtual machine associated with the class object
 * @param _referenceType The reference type for this class object
 */
class PureClassObjectInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _infoProducer: InfoProducerProfile,
  private val _classObjectReference: ClassObjectReference
)(
  override protected val _virtualMachine: VirtualMachine = _classObjectReference.virtualMachine(),
  private val _referenceType: ReferenceType = _classObjectReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, _infoProducer, _classObjectReference)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with ClassObjectInfoProfile {
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
  override def toJavaInfo: ClassObjectInfoProfile = {
    infoProducer.toJavaInfo.newClassObjectInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      classObjectReference = _classObjectReference
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
  override def toJdiInstance: ClassObjectReference = _classObjectReference

  /**
   * Retrieves the reference type information corresponding to this class
   * object.
   *
   * @return The reference type information
   */
  override def reflectedType: ReferenceTypeInfoProfile = {
    newTypeProfile(_classObjectReference.reflectedType()).toReferenceType
  }
}
