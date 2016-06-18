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
 * @param _threadReference The thread associated with the class object
 *                        (for method invocation)
 * @param _referenceType The reference type for this class object
 */
class PureClassObjectInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _infoProducer: InfoProducerProfile,
  private val _classObjectReference: ClassObjectReference
)(
  override protected val _virtualMachine: VirtualMachine = _classObjectReference.virtualMachine(),
  private val _threadReference: ThreadReference = _classObjectReference.owningThread(),
  private val _referenceType: ReferenceType = _classObjectReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, _infoProducer, _classObjectReference)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) with ClassObjectInfoProfile {
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
